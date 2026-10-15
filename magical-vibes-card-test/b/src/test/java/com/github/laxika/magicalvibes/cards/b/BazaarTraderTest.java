package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BazaarTrader.class, Forest.class, GrizzlyBears.class, Millstone.class, Pacifism.class, Threaten.class})
class BazaarTraderTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains control of a creature you control")
    void gainsControlOfCreature() {
        Permanent trader = addTrader();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        activateFor(creature);

        assertThat(trader.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Target player gains control of an artifact or land you control")
    void gainsControlOfArtifactAndLand() {
        addTrader();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        activateFor(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);

        Permanent secondTrader = addTrader();
        int traderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(secondTrader);
        harness.activateAbilityWithMultiTargets(player1, traderIndex, 0, List.of(player2.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(secondTrader.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("A permanent you control must be an artifact, creature, or land")
    void rejectsOtherPermanentTypes() {
        Permanent trader = addTrader();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(trader.getId());

        assertThatThrownBy(() -> activateFor(aura))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land you control");
    }

    @Test
    @DisplayName("Bazaar Trader can give itself away and remains tapped")
    void canGiveItselfAway() {
        Permanent trader = addTrader();

        activateFor(trader);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(trader);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(trader);
        assertThat(trader.isTapped()).isTrue();
        assertThat(trader.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Targeting yourself does not reset a creature's readiness")
    void canTargetYourself() {
        Permanent trader = addTrader();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(trader.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("Cannot give away a permanent controlled by the opponent")
    void rejectsOpponentsPermanent() {
        addTrader();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> activateFor(creature))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("A newly entered Bazaar Trader cannot pay its tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent trader = harness.addToBattlefieldAndReturn(player1, new BazaarTrader());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> activateFor(creature))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trader.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("A permanent no longer controlled by the activator is not transferred")
    void rechecksControlOnResolution() {
        addTrader();
        Permanent secondTrader = addTrader();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), creature.getId()));
        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(secondTrader), 0,
                List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Targeting yourself keeps a temporarily stolen creature after cleanup")
    void keepsTemporarilyStolenCreature() {
        Permanent trader = addTrader();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(trader), 0,
                List.of(player1.getId(), creature.getId()));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    private Permanent addTrader() {
        return addCreatureReady(player1, new BazaarTrader());
    }

    private void activateFor(Permanent target) {
        int traderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Bazaar Trader"));
        harness.activateAbilityWithMultiTargets(player1, traderIndex, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();
    }
}
