package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FirebenderAscension.class, FireSages.class, GrizzlyBears.class})
class FirebenderAscensionTest extends BaseCardTest {

    @Test
    void enteringCreatesSoldierWithFirebending() {
        harness.setHand(player1, List.of(new FirebenderAscension()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent soldier = findPermanents(player1, "Soldier").getFirst();
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
        Permanent fireSages = harness.addToBattlefieldAndReturn(player1, new FireSages());
        fireSages.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(fireSages)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void fourthQuestCounterOffersToCopyTheTriggeredAbility() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new FirebenderAscension());
        ascension.setCounterCount(CounterType.QUEST, 3);
        Permanent fireSages = harness.addToBattlefieldAndReturn(player1, new FireSages());
        fireSages.setSummoningSick(false);

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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }
}
