package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KjeldoranOutrider;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalduvianRage.class, KjeldoranOutrider.class})
class BalduvianRageTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a target attacking creature +X/+0 and schedules a draw")
    void boostsAttackerAndSchedulesDraw() {
        Permanent attacker = addCreatureReady(player2, new KjeldoranOutrider());
        attacker.setAttacking(true);
        castRage(attacker, 3);
        GameData gd = harness.getGameData();

        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent attacker = addCreatureReady(player2, new KjeldoranOutrider());
        attacker.setAttacking(true);
        castRage(attacker, 2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        Permanent attacker = addCreatureReady(player2, new KjeldoranOutrider());
        attacker.setAttacking(true);
        castRage(attacker, 1);
        GameData gd = harness.getGameData();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        Permanent creature = addCreatureReady(player2, new KjeldoranOutrider());
        prepareRage(1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Allows X to be zero")
    void allowsZeroX() {
        Permanent attacker = addCreatureReady(player2, new KjeldoranOutrider());
        attacker.setAttacking(true);
        castRage(attacker, 0);

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Does not resolve if the target stops attacking")
    void doesNotResolveIfTargetStopsAttacking() {
        Permanent attacker = addCreatureReady(player2, new KjeldoranOutrider());
        attacker.setAttacking(true);
        prepareRage(1);

        harness.castInstant(player1, 0, 1, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    private void prepareRage(int xValue) {
        harness.setHand(player1, List.of(new BalduvianRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        if (xValue > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, xValue);
        }
    }

    private void castRage(Permanent target, int xValue) {
        prepareRage(xValue);
        harness.castInstant(player1, 0, xValue, target.getId());
        harness.passBothPriorities();
    }
}
