package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonMask.class, GrizzlyBears.class})
class DragonMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature you control +2/+2 until end of turn")
    void pumpsTargetCreature() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new DragonMask());

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Target creature is returned to its owner's hand at the beginning of the next end step")
    void returnsTargetToHandAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent mask = harness.addToBattlefieldAndReturn(player1, new DragonMask());

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        // Still on the battlefield during the main phase.
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Advance to the end step — the creature should be bounced to its owner's hand.
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new DragonMask());

        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent you control")
    void cannotTargetOwnNoncreaturePermanent() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new DragonMask());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mask.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return a new object if the target left the battlefield")
    void doesNotReturnNewObjectAfterTargetLeavesAndReenters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addToBattlefieldAndReturn(player1, new DragonMask());
        Permanent original = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, original.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, original));
        Permanent replacement = addCreatureReady(player1, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, original.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, replacement.getId())).isSameAs(replacement);
        assertThat(gd.playerHands.get(player1.getId())).contains(original.getCard());
    }
}
