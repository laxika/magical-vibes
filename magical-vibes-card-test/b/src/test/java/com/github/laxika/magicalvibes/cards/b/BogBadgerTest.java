package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BogBadger.class})
class BogBadgerTest extends BaseCardTest {

    @Test
    void castWithoutKickerDoesNotGrantMenace() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BogBadger());
        harness.setHand(player1, List.of(new BogBadger()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent badger = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, badger, Keyword.MENACE)).isFalse();
    }

    @Test
    void kickedCastGrantsMenaceToYourCreaturesUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BogBadger());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BogBadger());
        harness.setHand(player1, List.of(new BogBadger()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent badger = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, badger, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.MENACE)).isFalse();

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, badger, Keyword.MENACE)).isFalse();
    }

    @Test
    void creatureEnteringBeforeKickedTriggerResolvesGainsMenace() {
        harness.setHand(player1, List.of(new BogBadger()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent badger = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, badger, Keyword.MENACE)).isFalse();
        Permanent arrivingCreature = harness.addToBattlefieldAndReturn(player1, new BogBadger());

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, badger, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, arrivingCreature, Keyword.MENACE)).isTrue();
    }

    @Test
    void creatureEnteringAfterKickedTriggerResolvesDoesNotGainMenace() {
        harness.setHand(player1, List.of(new BogBadger()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent badger = gd.playerBattlefields.get(player1.getId()).getLast();
        Permanent arrivingCreature = harness.addToBattlefieldAndReturn(player1, new BogBadger());

        assertThat(gqs.hasKeyword(gd, badger, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, arrivingCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    void unkickedCastDoesNotPutAnEntryTriggerOnTheStack() {
        harness.setHand(player1, List.of(new BogBadger()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bog Badger");
        assertThat(gd.stack).isEmpty();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
