package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AmrouKithkin;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtweftLieutenant.class, GrizzlyBears.class, AmrouKithkin.class, Conspiracy.class})
class ThoughtweftLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry lets a creature you control get +1/+1 and trample")
    void ownEntryTriggersAbility() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new ThoughtweftLieutenant(), "{G}{W}");
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

        harness.castFromHand(player1, new AmrouKithkin(), "{W}{W}");
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

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
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

        harness.castFromHand(player1, new AmrouKithkin(), "{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Grizzly Bears").getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost and trample wear off at cleanup")
    void boostAndTrampleWearOffAtCleanup() {
        setupLieutenantWithCreature();
        Permanent recipient = findPermanent(player1, "Grizzly Bears");

        harness.castFromHand(player1, new AmrouKithkin(), "{W}{W}");
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

    @Test
    @DisplayName("Its own entry can target itself")
    void ownEntryCanTargetItself() {
        harness.castFromHand(player1, new ThoughtweftLieutenant(), "{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent lieutenant = findPermanent(player1, "Thoughtweft Lieutenant");
        harness.handlePermanentChosen(player1, lieutenant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lieutenant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lieutenant)).isEqualTo(3);
        assertThat(lieutenant.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Kithkin entering does not trigger the ability")
    void opponentKithkinDoesNotTriggerAbility() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new ThoughtweftLieutenant());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AmrouKithkin(), "{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, lieutenant)).isEqualTo(2);
        assertThat(lieutenant.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Its own entry triggers even when Conspiracy replaces its Kithkin type")
    void ownEntryTriggersWithoutKithkinType() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new ThoughtweftLieutenant(), "{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recipient)).isEqualTo(3);
        assertThat(recipient.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The trigger still resolves after the Lieutenant leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        setupLieutenantWithCreature();
        Permanent lieutenant = findPermanent(player1, "Thoughtweft Lieutenant");
        Permanent recipient = findPermanent(player1, "Grizzly Bears");

        harness.castFromHand(player1, new AmrouKithkin(), "{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        gd.playerBattlefields.get(player1.getId()).remove(lieutenant);
        gd.playerGraveyards.get(player1.getId()).add(lieutenant.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recipient)).isEqualTo(3);
        assertThat(recipient.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    private void setupLieutenantWithCreature() {
        harness.addToBattlefield(player1, new ThoughtweftLieutenant());
        harness.addToBattlefield(player1, new GrizzlyBears());
    }
}
