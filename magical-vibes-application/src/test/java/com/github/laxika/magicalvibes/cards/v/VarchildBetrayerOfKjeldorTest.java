package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CunningSurvivor;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VarchildBetrayerOfKjeldor.class, CunningSurvivor.class, LightningBolt.class})
class VarchildBetrayerOfKjeldorTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage gives the damaged player that many Survivor tokens")
    void combatDamageCreatesSurvivorsForDamagedPlayer() {
        Permanent varchild = addCreatureReady(player1, new VarchildBetrayerOfKjeldor());
        varchild.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(survivorsControlledBy(player2)).hasSize(3);
        assertThat(survivorsControlledBy(player1)).isEmpty();
    }

    @Test
    @DisplayName("Opposing Survivors can't block")
    void opposingSurvivorCannotBlock() {
        Permanent varchild = addCreatureReady(player1, new VarchildBetrayerOfKjeldor());
        Permanent survivor = addCreatureReady(player2, new CunningSurvivor());
        varchild.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Survivors your opponents control can't block");
        assertThat(survivor.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Opposing Survivors can't attack Varchild's controller")
    void opposingSurvivorCannotAttackController() {
        addCreatureReady(player1, new VarchildBetrayerOfKjeldor());
        addCreatureReady(player2, new CunningSurvivor());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack");
    }

    @Test
    @DisplayName("Leaving the battlefield gives control of all Survivors to Varchild's controller")
    void gainsControlOfSurvivorsWhenLeaving() {
        Permanent varchild = harness.addToBattlefieldAndReturn(player1, new VarchildBetrayerOfKjeldor());
        Permanent survivor = addCreatureReady(player2, new CunningSurvivor());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, varchild.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(survivor);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(survivor);
    }

    private List<Permanent> survivorsControlledBy(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> "Survivor".equals(permanent.getCard().getName()))
                .toList();
    }
}
