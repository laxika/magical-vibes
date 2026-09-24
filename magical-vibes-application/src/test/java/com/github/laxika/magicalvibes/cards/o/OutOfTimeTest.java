package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutOfTime.class, Disenchant.class, GrizzlyBears.class})
class OutOfTimeTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and phases out all creatures, counting the creatures phased out")
    void untapsAndPhasesOutAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownCreature.tap();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingCreature.tap();

        castAndResolveOutOfTime();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(ownCreature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opposingCreature);
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Out of Time").getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @DisplayName("Keeps creatures phased out through their next untap step")
    void keepsCreaturesPhasedOutUntilItLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolveOutOfTime();

        advanceToUntap(player2);

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Phases creatures in tapped when it leaves the battlefield")
    void phasesCreaturesInTappedWhenItLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolveOutOfTime();
        Permanent outOfTime = findPermanent(player1, "Out of Time");

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player1, 0, outOfTime.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Out of Time");
    }

    @Test
    @DisplayName("Removes a time counter during upkeep and sacrifices on the last one")
    void vanishingRemovesCountersAndSacrificesOnLastCounter() {
        Permanent outOfTime = harness.addToBattlefieldAndReturn(player1, new OutOfTime());
        outOfTime.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Out of Time");
        harness.assertInGraveyard(player1, "Out of Time");
    }

    private void castAndResolveOutOfTime() {
        harness.setHand(player1, List.of(new OutOfTime()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToUntap(com.github.laxika.magicalvibes.model.Player player) {
        harness.performUntapStep(player);
    }
}
