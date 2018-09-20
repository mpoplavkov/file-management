package services

import java.io.File
import java.util.UUID

import com.amazonaws.SdkBaseException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.S3Object
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import play.api.Configuration

class S3FileManagerTest extends FunSuite with MockFactory {

  val file = new File("some_file")
  val id: UUID = UUID.randomUUID()
  val bucketName = "BUCKET_NAME"
  val exception = new SdkBaseException("sdk exception")
  val conf = Configuration("aws.s3.bucket.name" -> bucketName)
  val client: AmazonS3 = mock[AmazonS3]
  val manager = new S3FileManager(client, conf)


  test("should upload a file") {
    (client.putObject(_: String, _: String, _: File)).expects(bucketName, *, file)
    manager.upload(file)
  }

  test("should return exception when it occurs while uploading") {
    (client.putObject(_: String, _: String, _: File)).expects(bucketName, *, file).throwing(exception)
    manager.upload(file) shouldBe Left(exception)
  }

  test("should delete existing file") {
    (client.doesObjectExist(_: String, _: String)).expects(bucketName, id.toString).returning(true)
    (client.deleteObject(_: String, _: String)).expects(bucketName, id.toString)
    manager.remove(id) shouldBe Right(true)
  }

  test("should return false when deletes nonexistent file") {
    (client.doesObjectExist(_: String, _: String)).expects(bucketName, id.toString).returning(false)
    manager.remove(id) shouldBe Right(false)
  }

  test("should return exception when it occurs while deleting") {
    (client.doesObjectExist(_: String, _: String)).expects(bucketName, id.toString).throwing(exception)
    manager.remove(id) shouldBe Left(exception)
  }

  test("should download a file") {
    val obj = mock[S3Object]
    (client.getObject(_: String, _: String)).expects(bucketName, id.toString).returning(obj)
    (obj.getObjectContent _).expects()
    manager.download(id)
  }

  test("should return exception when it occurs while downloading") {
    (client.getObject(_: String, _: String)).expects(bucketName, id.toString).throwing(exception)
    manager.download(id) shouldBe Left(exception)
  }

}