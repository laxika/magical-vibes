package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AmrouKithkin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThoughtweftLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry lets a creature you control get +1/+1 and trample")
    void ownEntryTriggersAbility() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThoughtweftLieutenant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recipient)).isEqualTo(3);
        assertThat(recipient.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Another Kithkin entering triggers the ability")
    void anotherKithkinEntryTriggersAbility() {
        harness.addToBattlefield(player1, new ThoughtweftLieutenant());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new AmrouKithkin()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recipient)).isEqualTo(3);
        assertThat(recipient.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A non-Kithkin entering does not trigger the ability")
    void nonKithkinEntryDoesNotTriggerAbility() {
        harness.addToBattlefield(player1, new ThoughtweftLieutenant());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, recipient)).isEqualTo(2);
        assertThat(recipient.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The trigger cannot target an opponent's creature")
    void triggerCannotTargetOpponentCreature() {
        setupLieutenantWithCreature();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new AmrouKithkin()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost and trample wear off at cleanup")
    void boostAndTrampleWearOffAtCleanup() {
        setupLieutenantWithCreature();
        Permanent recipient = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new AmrouKithkin()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, recipient)).isEqualTo(2);
        assertThat(recipient.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private void setupLieutenantWithCreature() {
        harness.addToBattlefield(player1, new ThoughtweftLieutenant());
        harness.addToBattlefield(player1, new GrizzlyBears());
    }
}
