package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CinderingCutthroat.class})
class CinderingCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter when an opponent lost life this turn")
    void entersWithCounterAfterOpponentLostLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        castCutthroat();

        Permanent cutthroat = findPermanent(player1, "Cindering Cutthroat");

        assertThat(cutthroat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not enter with a +1/+1 counter when no opponent lost life this turn")
    void entersWithoutCounterWhenNoOpponentLostLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castCutthroat();

        Permanent cutthroat = findPermanent(player1, "Cindering Cutthroat");

        assertThat(cutthroat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gains menace until end of turn for {1}{B/R}")
    void gainsMenaceUntilEndOfTurn() {
        Permanent cutthroat = addCreatureReady(player1, new CinderingCutthroat());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cutthroat.getGrantedKeywords()).contains(Keyword.MENACE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cutthroat.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }

    @Test
    @DisplayName("Controller losing life does not satisfy the entry condition")
    void controllerLifeLossDoesNotGiveCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.lifeLostThisTurn.put(player1.getId(), 3);

        castCutthroat();

        assertThat(findPermanent(player1, "Cindering Cutthroat")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Entry condition checks life lost before resolution, not before casting")
    void lifeLossWhileSpellIsOnStackGivesCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CinderingCutthroat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        gd.lifeLostThisTurn.put(player2.getId(), 5);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cindering Cutthroat")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Menace can be paid for with red mana and is gained only on resolution")
    void gainsMenaceWithRedManaOnResolution() {
        Permanent cutthroat = addCreatureReady(player1, new CinderingCutthroat());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(cutthroat.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
        harness.passBothPriorities();
        assertThat(cutthroat.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    private void castCutthroat() {
        harness.setHand(player1, List.of(new CinderingCutthroat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
