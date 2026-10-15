package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ServoSchematic.class})
class ServoSchematicTest extends BaseCardTest {

    @Test
    void createsServoWhenItEnters() {
        harness.setHand(player1, List.of(new ServoSchematic()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertServoTokens(1);
    }

    @Test
    void createsServoWhenPutIntoGraveyardFromBattlefield() {
        Permanent schematic = harness.addToBattlefieldAndReturn(player1, new ServoSchematic());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, schematic));
        harness.passBothPriorities();

        assertServoTokens(1);
        harness.assertInGraveyard(player1, "Servo Schematic");
    }

    private void assertServoTokens(int expectedCount) {
        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();

        assertThat(servos).hasSize(expectedCount);
        assertThat(servos).allSatisfy(servo -> {
            assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(servo.getCard().getColor()).isNull();
            assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
        });
    }

    @Test
    void bothTriggersResolveWhenRemovedBeforeEntryTriggerResolves() {
        Permanent schematic = harness.enterBattlefieldAndReturn(player1, new ServoSchematic());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, schematic));
        resolveAllTriggers();

        assertServoTokens(2);
        harness.assertInGraveyard(player1, "Servo Schematic");
    }

    @Test
    void exilingDoesNotCreateServo() {
        Permanent schematic = harness.addToBattlefieldAndReturn(player1, new ServoSchematic());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, schematic));
        resolveAllTriggers();

        assertServoTokens(0);
        assertThat(gd.findExiledCard(schematic.getCard().getId())).isNotNull();
    }

    @Test
    void returningToHandDoesNotCreateServo() {
        Permanent schematic = harness.addToBattlefieldAndReturn(player1, new ServoSchematic());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, schematic));
        resolveAllTriggers();

        assertServoTokens(0);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName()).contains("Servo Schematic");
    }

    @Test
    void graveyardTriggerCreatesServoForLastControllerRatherThanOwner() {
        ServoSchematic card = new ServoSchematic();
        card.setOwnerId(player1.getId());
        Permanent schematic = harness.addToBattlefieldAndReturn(player2, card);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, schematic));
        resolveAllTriggers();

        assertServoTokens(0);
        assertThat(findPermanents(player2, "Servo")).hasSize(1);
        harness.assertInGraveyard(player1, "Servo Schematic");
    }
}
