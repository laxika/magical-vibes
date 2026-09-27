package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MyrSire;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrimazBlightOfOreskos.class, BasilicaShepherd.class, MyrSire.class, GrizzlyBears.class})
class BrimazBlightOfOreskosTest extends BaseCardTest {

    @Test
    void incubatesForTheManaValueOfAPhyrexianCreatureSpell() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        harness.setHand(player1, List.of(new BasilicaShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator).isNotNull();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void incubatesForTheManaValueOfAnArtifactCreatureSpell() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        harness.setHand(player1, List.of(new MyrSire()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator).isNotNull();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotIncubateForAnUnqualifiedCreatureSpell() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void proliferatesAtEachEndStepAfterAPhyrexianDiesUnderYourControl() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.creatureSubtypeDeathCountThisTurn.put(
                player1.getId(), Map.of(CardSubtype.PHYREXIAN, 1));

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void doesNotProliferateWhenOnlyANonPhyrexianCreatureDied() {
        harness.addToBattlefield(player1, new BrimazBlightOfOreskos());
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
