package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PurpleWorm.class, GrizzlyBears.class, Shock.class, GiantGrowth.class, MoggFanatic.class})
class PurpleWormTest extends BaseCardTest {

    @Test
    @DisplayName("Costs two less to cast after a creature dies this turn")
    void costsLessAfterCreatureDies() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new Shock(), new PurpleWorm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Purple Worm");
    }

    @Test
    @DisplayName("Cannot use the reduced cost when no creature has died")
    void doesNotGetCostReductionWithoutCreatureDeath() {
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new PurpleWorm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay two")
    void wardCountersUnpaidSpell() {
        Permanent worm = addReadyWorm(player1);
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castInstant(player2, 0, worm.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Giant Growth");
        harness.assertOnBattlefield(player1, "Purple Worm");
    }

    @Test
    @DisplayName("Paying two lets an opponent's spell targeting Purple Worm resolve")
    void payingWardCostLetsSpellResolve() {
        Permanent worm = addReadyWorm(player1);
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castInstant(player2, 0, worm.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, worm)).isEqualTo(11);
    }

    @Test
    @DisplayName("Can pay the full cost when no creature has died")
    void fullCostWithoutCreatureDeath() {
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new PurpleWorm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Purple Worm");
    }

    @Test
    @DisplayName("A creature card already in a graveyard does not enable the reduction")
    void creatureCardInGraveyardIsNotADeath() {
        prepareMainPhase(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new PurpleWorm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing your own creature enables the reduced cost")
    void ownSacrificedCreatureEnablesReduction() {
        prepareMainPhase(player1);
        harness.addToBattlefield(player1, new MoggFanatic());
        harness.setHand(player1, List.of(new PurpleWorm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Purple Worm");
    }

    @Test
    @DisplayName("Ward does not trigger for its controller's spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent worm = addReadyWorm(player1);
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, worm.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, worm)).isEqualTo(11);
    }

    @Test
    @DisplayName("Ward counters an opponent's activated ability even after its source is sacrificed")
    void wardCountersActivatedAbility() {
        Permanent worm = addReadyWorm(player1);
        prepareMainPhase(player2);
        harness.addToBattlefield(player2, new MoggFanatic());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, worm.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(worm.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Mogg Fanatic");
    }

    @Test
    @DisplayName("Paying ward allows an opponent's activated ability to resolve")
    void payingWardForActivatedAbility() {
        Permanent worm = addReadyWorm(player1);
        prepareMainPhase(player2);
        harness.addToBattlefield(player2, new MoggFanatic());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, worm.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(worm.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple creature deaths still reduce the generic cost by only two")
    void multipleDeathsDoNotIncreaseReduction() {
        prepareMainPhase(player1);
        harness.addToBattlefield(player1, new MoggFanatic());
        harness.addToBattlefield(player1, new MoggFanatic());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new PurpleWorm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Purple Worm");
    }

    private Permanent addReadyWorm(Player player) {
        Permanent worm = harness.addToBattlefieldAndReturn(player, new PurpleWorm());
        worm.setSummoningSick(false);
        return worm;
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
