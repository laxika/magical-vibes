package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CaptainAmericaTeamLeader;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WinterSoldierRebornAvenger.class, CaptainAmericaTeamLeader.class, GrizzlyBears.class, HillGiant.class,
        WoodlandChangeling.class})
class WinterSoldierRebornAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking returns a legal Hero with a +1/+1 counter")
    void attackingReturnsHeroWithCounter() {
        addCreatureReady(player1, new WinterSoldierRebornAvenger());
        Card hero = new CaptainAmericaTeamLeader();
        harness.setGraveyard(player1, List.of(hero));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(hero.getId());

        harness.handleMultipleCardsChosen(player1, List.of(hero.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, hero.getName());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking excludes creatures whose mana value exceeds Winter Soldier's power")
    void attackingExcludesTooExpensiveCreature() {
        addCreatureReady(player1, new WinterSoldierRebornAvenger());
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
    }

    @Test
    @DisplayName("Attacking returns a non-Hero creature without a counter")
    void attackingReturnsNonHeroWithoutCounter() {
        addCreatureReady(player1, new WinterSoldierRebornAvenger());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, creature.getName());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A changeling is a Hero and returns with an additional counter")
    void attackingReturnsChangelingWithCounter() {
        addCreatureReady(player1, new WinterSoldierRebornAvenger());
        Card creature = new WoodlandChangeling();
        harness.setGraveyard(player1, List.of(creature));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, creature.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("The return uses Winter Soldier's last known power after it leaves")
    void returnsCreatureAfterSourceLeaves() {
        Permanent soldier = addCreatureReady(player1, new WinterSoldierRebornAvenger());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        });
        assertThat(gd.stack).isNotEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, soldier));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, creature.getName());
        harness.assertNotInGraveyard(player1, creature.getName());
    }

    @Test
    @DisplayName("A target becomes illegal when Winter Soldier's power falls below its mana value")
    void powerDecreaseMakesTargetIllegal() {
        Permanent soldier = addCreatureReady(player1, new WinterSoldierRebornAvenger());
        soldier.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        });
        assertThat(gd.stack).isNotEmpty();
        soldier.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, creature.getName());
        harness.assertNotOnBattlefield(player1, creature.getName());
    }
}
