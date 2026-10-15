package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NivMizzetVisionary.class, Shock.class})
class NivMizzetVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws the amount of noncombat damage dealt by a source you control to an opponent")
    void drawsDamageAmountFromControlledSource() {
        harness.addToBattlefield(player1, new NivMizzetVisionary());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger for noncombat damage from an opponent's source")
    void doesNotTriggerForOpponentControlledSource() {
        harness.addToBattlefield(player1, new NivMizzetVisionary());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Controller keeps more than seven cards during cleanup")
    void controllerHasNoMaximumHandSize() {
        harness.addToBattlefield(player1, new NivMizzetVisionary());
        harness.setHand(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.inMutationScope(() -> gs.advanceStep(gd));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }

    @Test
    @DisplayName("Opponent still discards to seven during cleanup")
    void opponentStillHasMaximumHandSize() {
        harness.addToBattlefield(player1, new NivMizzetVisionary());
        harness.setHand(player2, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.inMutationScope(() -> gs.advanceStep(gd));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Controlled source dealing damage to its controller does not draw cards")
    void doesNotTriggerForDamageToSelf() {
        harness.addToBattlefield(player1, new NivMizzetVisionary());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage to an opponent's creature does not draw cards")
    void doesNotTriggerForDamageToCreature() {
        harness.addToBattlefield(player1, new NivMizzetVisionary());
        var target = harness.addToBattlefieldAndReturn(player2, new NivMizzetVisionary());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to an opponent does not draw cards")
    void doesNotTriggerForCombatDamage() {
        var attacker = harness.addToBattlefieldAndReturn(player1, new NivMizzetVisionary());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 15);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
