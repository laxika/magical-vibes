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
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SilhanaStarfletcher());

        prepareDeclareBlockers(player1);

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
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TorchDrake());

        prepareDeclareBlockers(player1);
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
        harness.clearPriorityPassed();
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
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, ledgewalker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
