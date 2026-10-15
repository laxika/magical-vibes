package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SequenceEngine.class, GrizzlyBears.class, Shock.class, Ornithopter.class})
class SequenceEngineTest extends BaseCardTest {

    @Test
    void exilesMatchingCreatureAndCreatesFractalWithXCounters() {
        Permanent engine = addReadyEngine();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareSorcerySpeed();

        harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(fractal.getEffectivePower()).isEqualTo(2);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(2);
        assertThat(engine.isTapped()).isTrue();
    }

    @Test
    void cannotTargetCreatureWithDifferentManaValue() {
        addReadyEngine();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareSorcerySpeed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNonCreatureCardOrActivateAtInstantSpeed() {
        addReadyEngine();
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareSorcerySpeed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, shock.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, gd.playerGraveyards.get(player2.getId()).getFirst().getId(),
                Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canExileCreatureFromOwnGraveyard() {
        addReadyEngine();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareSorcerySpeed();

        harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
        assertThat(findPermanents(player2, "Fractal")).isEmpty();
    }

    @Test
    void zeroManaValueTargetIsExiledAndZeroToughnessFractalDies() {
        addReadyEngine();
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player2, List.of(ornithopter));
        prepareSorcerySpeed();

        harness.activateAbility(player1, 0, 0, ornithopter.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(ornithopter);
        assertThat(findPermanents(player1, "Fractal")).isEmpty();
    }

    @Test
    void createsNoTokenWhenTargetLeavesGraveyardBeforeResolution() {
        addReadyEngine();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareSorcerySpeed();

        harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(bears));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(bears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(bears);
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent engine = addReadyEngine();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareSorcerySpeed();

        harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).remove(engine);
        harness.setGraveyard(player1, List.of(engine.getCard()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void cannotActivateWithNonemptyStackEvenDuringMainPhase() {
        addReadyEngine();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareSorcerySpeed();
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Fractal")).isEmpty();
    }

    private Permanent addReadyEngine() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new SequenceEngine());
        engine.setSummoningSick(false);
        return engine;
    }

    private void prepareSorcerySpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
