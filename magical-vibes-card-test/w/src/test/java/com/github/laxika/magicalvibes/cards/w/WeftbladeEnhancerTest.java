package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeftbladeEnhancer.class, GrizzlyBears.class, Plains.class})
class WeftbladeEnhancerTest extends BaseCardTest {

    @Test
    void putsCounterOnOneTargetCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WeftbladeEnhancer()));
        addMana();

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castCreature(player1, 0, List.of(targetId));
        resolveAllTriggers();

        assertThat(findPermanentById(targetId).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void putsCounterOnEachOfTwoTargetCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WeftbladeEnhancer()));
        addMana();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        UUID firstId = battlefield.get(0).getId();
        UUID secondId = battlefield.get(1).getId();
        harness.castCreature(player1, 0, List.of(firstId, secondId));
        resolveAllTriggers();

        assertThat(findPermanentById(firstId).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(findPermanentById(secondId).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void canEnterWithoutTargets() {
        harness.setHand(player1, List.of(new WeftbladeEnhancer()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Weftblade Enhancer");
        assertThat(findPermanent(player1, "Weftblade Enhancer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new WeftbladeEnhancer()));
        addMana();

        UUID opponentLandId = harness.getPermanentId(player2, "Plains");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentLandId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void warpCastsForAlternateCostAndExilesAtNextEndStep() {
        WeftbladeEnhancer enhancer = new WeftbladeEnhancer();
        harness.setHand(player1, List.of(enhancer));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Weftblade Enhancer");
        resolveAllTriggers();

        assertThat(gd.findExiledCard(enhancer.getId())).isNotNull();
    }

    @Test
    @CardUsed(WeftbladeEnhancer.class)
    void canTargetItselfAfterEntering() {
        harness.setHand(player1, List.of(new WeftbladeEnhancer()));
        addMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        UUID enhancerId = harness.getPermanentId(player1, "Weftblade Enhancer");
        harness.handlePermanentChosen(player1, enhancerId);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Weftblade Enhancer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed(WeftbladeEnhancer.class)
    void canPutCountersOnCreaturesControlledByDifferentPlayers() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new WeftbladeEnhancer());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new WeftbladeEnhancer());
        harness.setHand(player1, List.of(new WeftbladeEnhancer()));
        addMana();

        harness.castCreature(player1, 0, List.of(own.getId(), opposing.getId()));
        resolveAllTriggers();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed(WeftbladeEnhancer.class)
    void normalCastRemainsOnBattlefieldAtEndStep() {
        harness.setHand(player1, List.of(new WeftbladeEnhancer()));
        addMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Weftblade Enhancer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(WeftbladeEnhancer.class)
    void warpRecastOnLaterTurnPaysNormalCostAndTriggersAgain() {
        WeftbladeEnhancer enhancer = new WeftbladeEnhancer();
        harness.setLibrary(player1, List.of(new WeftbladeEnhancer(), new WeftbladeEnhancer()));
        harness.setLibrary(player2, List.of(new WeftbladeEnhancer(), new WeftbladeEnhancer()));
        harness.setHand(player1, List.of(enhancer));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Weftblade Enhancer"));
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Weftblade Enhancer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Weftblade Enhancer");
        resolveAllTriggers();
        assertThat(gd.findExiledCard(enhancer.getId())).isNotNull();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        assertThatThrownBy(() -> harness.castFromExile(player1, enhancer.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, enhancer.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, enhancer.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Weftblade Enhancer"));
        resolveAllTriggers();

        assertThat(gd.findExiledCard(enhancer.getId())).isNull();
        assertThat(findPermanent(player1, "Weftblade Enhancer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Weftblade Enhancer");
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private Permanent findPermanentById(UUID id) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
