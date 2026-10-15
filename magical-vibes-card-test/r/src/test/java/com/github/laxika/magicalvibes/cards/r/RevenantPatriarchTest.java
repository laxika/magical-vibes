package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RevenantPatriarch.class})
class RevenantPatriarchTest extends BaseCardTest {

    @Test
    @DisplayName("Skips the targeted player's next combat phase when white mana was spent")
    void skipsCombatPhaseWhenWhiteManaWasSpent() {
        castRevenantPatriarch(ManaColor.WHITE, player2.getId());

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not skip a combat phase when white mana was not spent")
    void doesNotSkipCombatPhaseWithoutWhiteMana() {
        castRevenantPatriarch(ManaColor.COLORLESS, player2.getId());

        assertThat(gd.skipNextCombatPhaseCount).isEmpty();
    }

    @Test
    @DisplayName("May target either player")
    void mayTargetEitherPlayer() {
        castRevenantPatriarch(ManaColor.WHITE, player1.getId());

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot block")
    void cannotBlock() {
        Permanent blocker = addCreatureReady(player2, new RevenantPatriarch());

        assertThat(bls.canBlock(gd, blocker)).isFalse();
    }

    @Test
    @DisplayName("Skips exactly the next combat phase, then allows combat on the following turn")
    void skipsExactlyTheNextCombatPhase() {
        castRevenantPatriarch(ManaColor.WHITE, player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.skipNextCombatPhaseCount).isEmpty();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Entering without being cast does not trigger the combat skip")
    void enteringWithoutBeingCastDoesNotTrigger() {
        harness.enterBattlefieldAndReturn(player1, new RevenantPatriarch());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.skipNextCombatPhaseCount).isEmpty();
    }

    private void castRevenantPatriarch(ManaColor extraManaColor, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new RevenantPatriarch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, extraManaColor, 1);

        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();
    }
}
