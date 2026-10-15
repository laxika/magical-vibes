package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.TeoSpiritedGlider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirebenderAscension.class, FireSages.class, FireNationRaider.class, TeoSpiritedGlider.class})
class FirebenderAscensionTest extends BaseCardTest {

    @Test
    void enteringCreatesSoldierWithFirebending() {
        harness.setHand(player1, List.of(new FirebenderAscension()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Soldier");
        soldier.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(soldier)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void attackingTriggeredAbilityAddsQuestCounter() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new FirebenderAscension());
        Permanent fireSages = addCreatureReady(player1, new FireSages());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(fireSages)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void fourthQuestCounterOffersToCopyTheTriggeredAbility() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new FirebenderAscension());
        ascension.setCounterCount(CounterType.QUEST, 3);
        Permanent fireSages = addCreatureReady(player1, new FireSages());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(fireSages)));
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void attackingWithoutATriggeredAbilityDoesNotAddQuestCounter() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new FirebenderAscension());
        Permanent raider = addCreatureReady(player1, new FireNationRaider());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(raider)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void copyingCanBeDeclinedWithoutLosingTheQuestCounter() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new FirebenderAscension());
        ascension.setCounterCount(CounterType.QUEST, 3);
        addCreatureReady(player1, new FireSages());

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void opponentAttackTriggersDoNotAddQuestCounters() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new FirebenderAscension());
        addCreatureReady(player2, new FireSages());

        declareAttackers(player2, List.of(0));
        harness.passUntil(player2, TurnStep.END_OF_COMBAT);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void thirdQuestCounterDoesNotCopyTheAbility() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new FirebenderAscension());
        ascension.setCounterCount(CounterType.QUEST, 2);
        addCreatureReady(player1, new FireSages());

        declareAttackers(List.of(1));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void eachAttackingCreaturesOwnAbilityAddsAQuestCounter() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new FirebenderAscension());
        addCreatureReady(player1, new FireSages());
        addCreatureReady(player1, new FireSages());

        declareAttackers(List.of(1, 2));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void attackingCreatureWithOneOrMoreAttackTriggerAddsQuestCounter() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new FirebenderAscension());
        Permanent teo = addCreatureReady(player1, new TeoSpiritedGlider());
        harness.setLibrary(player1, List.of(new FireNationRaider()));

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, teo.getId());
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }
}
