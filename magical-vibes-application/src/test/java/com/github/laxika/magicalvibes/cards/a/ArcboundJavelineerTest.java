package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcboundJavelineer.class, BronzeSable.class, GrizzlyBears.class, Assassinate.class})
class ArcboundJavelineerTest extends BaseCardTest {

    @Test
    void entersWithOnePlusOneCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ArcboundJavelineer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent javelineer = findPermanent(player1, "Arcbound Javelineer");

        assertThat(javelineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void removesChosenCountersAndDealsThatMuchDamageToAttackingCreature() {
        Permanent javelineer = addCreatureReady(player1, new ArcboundJavelineer());
        javelineer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.activateAbility(player1, 0, 0, null, attacker.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue())
                .isEqualTo(3);
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        assertThat(javelineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetCreatureThatIsNotAttackingOrBlocking() {
        Permanent javelineer = addCreatureReady(player1, new ArcboundJavelineer());
        javelineer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(javelineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void modularMayPutItsCounterOnTargetArtifactCreatureWhenItDies() {
        Permanent javelineer = addCreatureReady(player1, new ArcboundJavelineer());
        javelineer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        javelineer.tap();
        Permanent bronzeSable = addCreatureReady(player1, new BronzeSable());

        destroyJavelineer(javelineer);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bronzeSable.getId());

        harness.handlePermanentChosen(player1, bronzeSable.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bronzeSable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void destroyJavelineer(Permanent javelineer) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, javelineer.getId(), null);
        harness.passBothPriorities();
    }
}
