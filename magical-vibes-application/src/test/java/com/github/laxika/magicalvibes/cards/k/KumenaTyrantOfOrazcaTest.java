package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MistCloakedHerald;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KumenaTyrantOfOrazca.class, MistCloakedHerald.class, Forest.class})
class KumenaTyrantOfOrazcaTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping another Merfolk makes Kumena unblockable this turn")
    void tapAnotherMerfolkMakesKumenaUnblockable() {
        Permanent kumena = addCreatureReady(player1, new KumenaTyrantOfOrazca());
        Permanent merfolk = addCreatureReady(player1, new MistCloakedHerald());

        harness.activateAbility(player1, battlefieldIndex(kumena), 0, null, null);
        harness.passBothPriorities();

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(kumena.isTapped()).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, kumena)).isTrue();
    }

    @Test
    @DisplayName("Tapping three Merfolk draws a card")
    void tapThreeMerfolkDrawsCard() {
        Permanent kumena = addCreatureReady(player1, new KumenaTyrantOfOrazca());
        Permanent merfolkA = addCreatureReady(player1, new MistCloakedHerald());
        Permanent merfolkB = addCreatureReady(player1, new MistCloakedHerald());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, battlefieldIndex(kumena), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(kumena.isTapped()).isTrue();
        assertThat(merfolkA.isTapped()).isTrue();
        assertThat(merfolkB.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping five Merfolk puts a +1/+1 counter on each Merfolk")
    void tapFiveMerfolkPutsCountersOnEachMerfolk() {
        Permanent kumena = addCreatureReady(player1, new KumenaTyrantOfOrazca());
        Permanent merfolkA = addCreatureReady(player1, new MistCloakedHerald());
        Permanent merfolkB = addCreatureReady(player1, new MistCloakedHerald());
        Permanent merfolkC = addCreatureReady(player1, new MistCloakedHerald());
        Permanent merfolkD = addCreatureReady(player1, new MistCloakedHerald());

        harness.activateAbility(player1, battlefieldIndex(kumena), 2, null, null);
        harness.passBothPriorities();

        assertThat(kumena.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(merfolkA.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(merfolkB.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(merfolkC.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(merfolkD.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability cannot be activated without another Merfolk")
    void cannotTapKumenaForItsOwnFirstAbility() {
        Permanent kumena = addCreatureReady(player1, new KumenaTyrantOfOrazca());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kumena), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summoningSickMerfolkCanPayDrawCost() {
        Permanent kumena = harness.addToBattlefieldAndReturn(player1, new KumenaTyrantOfOrazca());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MistCloakedHerald());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MistCloakedHerald());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, battlefieldIndex(kumena), 1, null, null);

        assertThat(kumena.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void tappedKumenaCanActivateFirstAbilityUsingSummoningSickMerfolk() {
        Permanent kumena = harness.addToBattlefieldAndReturn(player1, new KumenaTyrantOfOrazca());
        kumena.tap();
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MistCloakedHerald());

        harness.activateAbility(player1, battlefieldIndex(kumena), 0, null, null);

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, kumena)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasCantBeBlocked(gd, kumena)).isTrue();
    }

    @Test
    void tappedAndOpposingMerfolkCannotPayFirstCost() {
        Permanent kumena = addCreatureReady(player1, new KumenaTyrantOfOrazca());
        Permanent tapped = addCreatureReady(player1, new MistCloakedHerald());
        tapped.tap();
        Permanent opponent = addCreatureReady(player2, new MistCloakedHerald());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kumena), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kumena.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, kumena)).isFalse();
    }

    @Test
    void insufficientMerfolkCannotPayDrawOrCounterCost() {
        Permanent kumena = addCreatureReady(player1, new KumenaTyrantOfOrazca());
        Permanent merfolk = addCreatureReady(player1, new MistCloakedHerald());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kumena), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kumena), 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kumena.isTapped()).isFalse();
        assertThat(merfolk.isTapped()).isFalse();
    }

    @Test
    void counterAbilityIncludesMerfolkEnteringBeforeResolutionAndExcludesOpponents() {
        Permanent kumena = addCreatureReady(player1, new KumenaTyrantOfOrazca());
        List<Permanent> payers = List.of(
                addCreatureReady(player1, new MistCloakedHerald()),
                addCreatureReady(player1, new MistCloakedHerald()),
                addCreatureReady(player1, new MistCloakedHerald()),
                addCreatureReady(player1, new MistCloakedHerald()));
        Permanent opponent = addCreatureReady(player2, new MistCloakedHerald());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, battlefieldIndex(kumena), 2, null, null);

        assertThat(kumena.isTapped()).isTrue();
        assertThat(payers).allSatisfy(p -> assertThat(p.isTapped()).isTrue());
        assertThat(kumena.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new MistCloakedHerald());
        harness.passBothPriorities();

        assertThat(kumena.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(payers).allSatisfy(p ->
                assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newcomer.isTapped()).isFalse();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void unblockableEffectExpiresAtEndOfTurn() {
        Permanent kumena = addCreatureReady(player1, new KumenaTyrantOfOrazca());
        addCreatureReady(player1, new MistCloakedHerald());
        harness.activateAbility(player1, battlefieldIndex(kumena), 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasCantBeBlocked(gd, kumena)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, kumena)).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
