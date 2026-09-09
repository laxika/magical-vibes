package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GhituFireEater;
import com.github.laxika.magicalvibes.cards.i.IronWill;
import com.github.laxika.magicalvibes.cards.p.Parch;
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

@CardUsed({DefenderOfLaw.class, GhituFireEater.class, IronWill.class, Parch.class})
class DefenderOfLawTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast during the opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new DefenderOfLaw(), "{2}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Defender of Law");
    }

    @Test
    @DisplayName("Red creature cannot block Defender of Law")
    void redCreatureCannotBlock() {
        Permanent defender = addCreatureReady(player1, new DefenderOfLaw());
        defender.setAttacking(true);

        addCreatureReady(player2, new GhituFireEater());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be targeted by a red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent defender = addCreatureReady(player2, new DefenderOfLaw());

        harness.setHand(player1, List.of(new Parch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, defender.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be targeted by a white instant")
    void canBeTargetedByWhiteInstant() {
        Permanent defender = addCreatureReady(player2, new DefenderOfLaw());

        harness.setHand(player1, List.of(new IronWill()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, defender.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(IronWill.class);
    }

    @Test
    @DisplayName("Prevents combat damage from a red creature")
    void preventsCombatDamageFromRedCreature() {
        Permanent attacker = addCreatureReady(player1, new GhituFireEater());
        attacker.setAttacking(true);

        Permanent defender = addCreatureReady(player2, new DefenderOfLaw());
        defender.setBlocking(true);
        defender.addBlockingTarget(0);

        resolveCombat(player1);

        harness.assertOnBattlefield(player2, "Defender of Law");
        harness.assertNotOnBattlefield(player1, "Ghitu Fire-Eater");
    }
}
