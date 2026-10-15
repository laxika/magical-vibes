package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.cards.c.Cytoshape;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Duskwalker.class, ArdentSoldier.class, DarigaazsAttendant.class, Cytoshape.class})
class DuskwalkerTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCountersOrFear() {
        harness.castFromHand(player1, new Duskwalker(), "{B}");
        harness.passBothPriorities();

        Permanent duskwalker = findDuskwalker();
        assertThat(duskwalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, duskwalker, Keyword.FEAR)).isFalse();
    }

    @Test
    void castWithKickerEntersWithTwoCountersAndFear() {
        harness.setHand(player1, List.of(new Duskwalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent duskwalker = findDuskwalker();
        assertThat(duskwalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, duskwalker, Keyword.FEAR)).isTrue();
    }

    @Test
    void castWithKickerRequiresFullAdditionalCost() {
        harness.setHand(player1, List.of(new Duskwalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedFearDoesNotWearOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new Duskwalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent duskwalker = findDuskwalker();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duskwalker, Keyword.FEAR)).isTrue();
    }

    @Test
    void kickedDuskwalkerCannotBeBlockedByNonblackNonartifactCreature() {
        Permanent duskwalker = castKickedDuskwalker();
        Permanent blocker = addCreatureReady(player2, new ArdentSoldier());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, duskwalker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    void kickedDuskwalkerCanBeBlockedByBlackCreature() {
        Permanent duskwalker = castKickedDuskwalker();
        Permanent blocker = addCreatureReady(player2, new Duskwalker());

        prepareDeclareBlockers();

        assertThatCode(() -> declareBlock(blocker, duskwalker))
                .doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void kickedDuskwalkerCanBeBlockedByArtifactCreature() {
        Permanent duskwalker = castKickedDuskwalker();
        Permanent blocker = addCreatureReady(player2, new DarigaazsAttendant());

        prepareDeclareBlockers();

        assertThatCode(() -> declareBlock(blocker, duskwalker))
                .doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void enteringWithoutBeingCastDoesNotGrantCountersOrFear() {
        Permanent duskwalker = harness.enterBattlefieldAndReturn(player1, new Duskwalker());

        assertThat(duskwalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, duskwalker, Keyword.FEAR)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unkickedDuskwalkerCanBeBlockedByNonblackNonartifactCreature() {
        harness.castFromHand(player1, new Duskwalker(), "{B}");
        harness.passBothPriorities();
        Permanent duskwalker = findDuskwalker();
        duskwalker.setSummoningSick(false);
        duskwalker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ArdentSoldier());
        prepareDeclareBlockers();

        assertThatCode(() -> declareBlock(blocker, duskwalker)).doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void kickedDuskwalkerKeepsFearWhenItBecomesACopyOfAnotherCreature() {
        harness.setHand(player1, List.of(new Duskwalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent duskwalker = findDuskwalker();
        Permanent soldier = addCreatureReady(player2, new ArdentSoldier());

        harness.setHand(player1, List.of(new Cytoshape()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, duskwalker.getId());
        harness.handlePermanentChosen(player1, soldier.getId());

        assertThat(gqs.hasKeyword(gd, duskwalker, Keyword.VIGILANCE)).isTrue();
        assertThat(duskwalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, duskwalker, Keyword.FEAR)).isTrue();
    }

    @Test
    void kickedCreatureBecomingADuskwalkerDoesNotGainFearWithoutEnteringAgain() {
        harness.setHand(player1, List.of(new ArdentSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent soldier = findPermanent(player1, "Ardent Soldier");
        Permanent duskwalker = addCreatureReady(player2, new Duskwalker());

        harness.setHand(player1, List.of(new Cytoshape()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, soldier.getId());
        harness.handlePermanentChosen(player1, duskwalker.getId());

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.VIGILANCE)).isFalse();
        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FEAR)).isFalse();
    }

    private Permanent castKickedDuskwalker() {
        harness.setHand(player1, List.of(new Duskwalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent duskwalker = findDuskwalker();
        duskwalker.setSummoningSick(false);
        duskwalker.setAttacking(true);
        return duskwalker;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent findDuskwalker() {
        return findPermanent(player1, "Duskwalker");
    }
}
