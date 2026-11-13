package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CoalitionWarbrute;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TeoSpiritedGlider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirebenderAscension.class, FireSages.class, FireNationRaider.class, TeoSpiritedGlider.class,
        GrizzlyBears.class, CoalitionWarbrute.class})
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
    void decayedTriggeringAsTheCreatureAttacksAddsQuestCounter() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new FirebenderAscension());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.getGrantedKeywords().add(Keyword.DECAYED);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        // Decayed's "when this creature attacks, sacrifice it at end of combat" is a triggered ability of the creature
        // its attacking caused to trigger.
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
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
        addCreatureReady(player1, new FireSages());
        harness.setHand(player1, List.of(new FireNationRaider()));
        harness.setLibrary(player1, List.of(new FireNationRaider()));

        declareAttackers(List.of(1));
        while (gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) == null
                && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.handleCardChosen(player1, 1);
        harness.handlePermanentChosen(player1, teo.getId());
        resolveAllTriggers();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(teo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void enlistingDoesNotAddQuestCounter() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new FirebenderAscension());
        Permanent warbrute = addCreatureReady(player1, new CoalitionWarbrute());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warbrute)));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        // Enlist's "when you do" triggers on the enlist tap (CR 702.154a), not on the creature attacking.
        assertThat(warbrute.getPowerModifier()).isEqualTo(2);
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }
}
