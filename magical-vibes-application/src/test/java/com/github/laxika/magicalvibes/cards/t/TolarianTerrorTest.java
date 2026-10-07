package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

@CardUsed({TolarianTerror.class, Shock.class, GrizzlyBears.class, GiantGrowth.class,
        MindRot.class, ProdigalPyromancer.class})
class TolarianTerrorTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1} less for each instant or sorcery card in its controller's graveyard")
    void costReductionCountsInstantAndSorceryCards() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new TolarianTerror()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce its cost for non-instant and non-sorcery cards")
    void costReductionIgnoresOtherCardTypes() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new TolarianTerror()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay {2}")
    void wardCountersUnpaidSpell() {
        addReadyTerror(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Tolarian Terror"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Paying {2} lets an opponent's spell targeting it resolve")
    void payingWardLetsSpellResolve() {
        addReadyTerror(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Tolarian Terror"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        Permanent terror = findPermanent(player1, "Tolarian Terror");
        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(8);
    }

    @Test
    void costReductionCountsSorceriesAlongsideInstants() {
        harness.setGraveyard(player1, List.of(new Shock(), new MindRot()));
        harness.setHand(player1, List.of(new TolarianTerror()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsGraveyardDoesNotReduceCost() {
        harness.setGraveyard(player2, List.of(new Shock(), new MindRot()));
        harness.setHand(player1, List.of(new TolarianTerror()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void excessReductionStillRequiresOneBlueMana() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock(),
                new Shock(), new MindRot(), new MindRot(), new MindRot()));
        harness.setHand(player1, List.of(new TolarianTerror()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void reductionCannotPayTheBlueRequirement() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock(),
                new Shock(), new MindRot(), new MindRot(), new MindRot()));
        harness.setHand(player1, List.of(new TolarianTerror()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void controllerCanTargetTerrorWithoutPayingWard() {
        addReadyTerror(player1);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Tolarian Terror"));

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Tolarian Terror"))).isEqualTo(8);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningWardCountersSpellEvenWhenPaymentIsAvailable() {
        addReadyTerror(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Tolarian Terror"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Tolarian Terror"))).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void wardCountersOpponentsActivatedAbilityWithoutCounteringItsSource() {
        addReadyTerror(player1);
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, harness.getPermanentId(player1, "Tolarian Terror"));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Tolarian Terror").getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
        assertThat(pyromancer.isTapped()).isTrue();
    }

    private Permanent addReadyTerror(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TolarianTerror());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
