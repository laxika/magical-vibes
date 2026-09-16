package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
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

@CardUsed({OptimusPrimeHero.class, OptimusPrimeAutobotLeader.class, GrizzlyBears.class, Murder.class})
class OptimusPrimeHeroTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsOptimusConvertedWithLivingMetal() {
        Permanent optimus = castConvertedOptimus();

        assertThat(optimus.isTransformed()).isTrue();
        assertThat(optimus.getCard()).isInstanceOf(OptimusPrimeAutobotLeader.class);
        assertThat(gqs.isCreature(gd, optimus)).isTrue();
        assertThat(gqs.hasKeyword(gd, optimus, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void bolstersOnEachEndStepAndChoosesLeastToughnessCreature() {
        addCreatureReady(player1, new OptimusPrimeHero());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void attackBolstersChosenCreatureGrantsTrampleAndConvertsAfterCombatDamage() {
        Permanent optimus = castConvertedOptimus();
        optimus.setSummoningSick(false);
        Permanent chosenBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chosenBear.getId(), otherBear.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenBear.getId()));

        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(chosenBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, chosenBear, Keyword.TRAMPLE)).isTrue();
        assertThat(optimus.isTransformed()).isFalse();
    }

    @Test
    void deathReturnsOptimusToTheBattlefieldConvertedUnderItsOwnersControl() {
        Permanent optimus = addCreatureReady(player1, new OptimusPrimeHero());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, optimus.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Optimus Prime, Autobot Leader");
        assertThat(returned).isNotSameAs(optimus);
        assertThat(returned.isTransformed()).isTrue();
        assertThat(returned.getCard()).isInstanceOf(OptimusPrimeAutobotLeader.class);
    }

    private Permanent castConvertedOptimus() {
        harness.setHand(player1, List.of(new OptimusPrimeHero()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Optimus Prime, Autobot Leader");
    }
}
