package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ApprenticeSorcerer;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({CommanderEesha.class, AirElemental.class, ApprenticeSorcerer.class, Shock.class})
class CommanderEeshaTest extends BaseCardTest {

    @Test
    @DisplayName("A creature cannot block Commander Eesha")
    void creatureCannotBlock() {
        Permanent eesha = addReady(player1, new CommanderEesha());
        eesha.setAttacking(true);
        Permanent blocker = addReady(player2, new AirElemental());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, eesha)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A creature ability cannot target Commander Eesha")
    void creatureAbilityCannotTargetEesha() {
        Permanent sorcerer = addReady(player1, new ApprenticeSorcerer());
        Permanent eesha = addReady(player2, new CommanderEesha());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, sorcerer), 0, null, eesha.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A noncreature spell can target Commander Eesha")
    void noncreatureSpellCanTargetEesha() {
        Permanent eesha = addReady(player2, new CommanderEesha());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, eesha.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
