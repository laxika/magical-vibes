package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DouseInGloom;
import com.github.laxika.magicalvibes.cards.p.PlaguedRusalka;
import com.github.laxika.magicalvibes.cards.t.TorchDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilhanaLedgewalker.class, SilhanaStarfletcher.class, TorchDrake.class,
        DouseInGloom.class, PlaguedRusalka.class})
class SilhanaLedgewalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Silhana Ledgewalker can't be blocked by a creature without flying")
    void cannotBeBlockedByNonFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new SilhanaLedgewalker());
        Permanent blocker = addCreatureReady(player2, new SilhanaStarfletcher());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Silhana Ledgewalker can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new SilhanaLedgewalker());
        Permanent blocker = addCreatureReady(player2, new TorchDrake());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Silhana Ledgewalker can't be targeted by an opponent's spell")
    void cannotBeTargetedByOpponentSpell() {
        Permanent ledgewalker = addCreatureReady(player1, new SilhanaLedgewalker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DouseInGloom()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, ledgewalker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Silhana Ledgewalker can't be targeted by an opponent's ability")
    void cannotBeTargetedByOpponentAbility() {
        Permanent ledgewalker = addCreatureReady(player1, new SilhanaLedgewalker());
        addCreatureReady(player2, new PlaguedRusalka());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, ledgewalker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Hexproof allows the controller's spell to target Silhana Ledgewalker")
    void canBeTargetedByControllerSpell() {
        Permanent ledgewalker = addCreatureReady(player1, new SilhanaLedgewalker());
        harness.setHand(player1, List.of(new DouseInGloom()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, ledgewalker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Silhana Ledgewalker");
        harness.assertNotOnBattlefield(player1, "Silhana Ledgewalker");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Hexproof allows the controller's ability to target Silhana Ledgewalker")
    void canBeTargetedByControllerAbility() {
        Permanent rusalka = addCreatureReady(player1, new PlaguedRusalka());
        Permanent ledgewalker = addCreatureReady(player1, new SilhanaLedgewalker());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, ledgewalker.getId());
        harness.handlePermanentChosen(player1, rusalka.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plagued Rusalka");
        harness.assertInGraveyard(player1, "Silhana Ledgewalker");
        harness.assertNotOnBattlefield(player1, "Silhana Ledgewalker");
    }

    @Test
    @DisplayName("Silhana Ledgewalker can block a creature without flying")
    void canBlockNonFlyingCreature() {
        addCreatureReady(player1, new PlaguedRusalka());
        Permanent blocker = addCreatureReady(player2, new SilhanaLedgewalker());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargets()).containsExactly(0);
    }
}
