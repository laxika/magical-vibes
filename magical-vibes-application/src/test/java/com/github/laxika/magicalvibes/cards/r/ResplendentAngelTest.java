package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResplendentAngelTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creates a 4/4 flying, vigilant Angel token when you gained 5 life this turn")
    void createsAngelTokenOnFiveLifeGained() {
        harness.addToBattlefield(player1, new ResplendentAngel());
        gd.lifeGainedThisTurn.put(player1.getId(), 5);

        advanceToEndStep(player1);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        var angels = findPermanents(player1, "Angel");
        assertThat(angels).hasSize(1);
        assertThat(angels).allSatisfy(t -> {
            assertThat(t.getCard().getPower()).isEqualTo(4);
            assertThat(t.getCard().getToughness()).isEqualTo(4);
            assertThat(t.getCard().isToken()).isTrue();
            assertThat(gqs.hasKeyword(gd, t, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, t, Keyword.VIGILANCE)).isTrue();
        });
    }

    @Test
    @DisplayName("Creates no token when you gained fewer than 5 life this turn")
    void noTokenBelowThreshold() {
        harness.addToBattlefield(player1, new ResplendentAngel());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers on each end step, including the opponent's")
    void triggersOnOpponentEndStep() {
        harness.addToBattlefield(player1, new ResplendentAngel());
        gd.lifeGainedThisTurn.put(player1.getId(), 5);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
    }

    @Test
    @DisplayName("Life gained by the opponent does not trigger the ability")
    void opponentLifeGainDoesNotTrigger() {
        harness.addToBattlefield(player1, new ResplendentAngel());
        gd.lifeGainedThisTurn.put(player2.getId(), 9);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isZero();
    }

    @Test
    @DisplayName("Activated ability gives +2/+2 and lifelink until end of turn")
    void activatedAbilityPumpsAndGrantsLifelink() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new ResplendentAngel());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.passBothPriorities(); // CLEANUP -> next turn

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.LIFELINK)).isFalse();
    }
}
