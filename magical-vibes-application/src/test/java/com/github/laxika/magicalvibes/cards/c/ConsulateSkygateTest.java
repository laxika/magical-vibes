package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class ConsulateSkygateTest extends BaseCardTest {

    @Test
    @DisplayName("Consulate Skygate can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent skygate = addReadySkygate(player2);
        Permanent flyer = addAttackingCreature(player1, new AirElemental());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(skygate),
                gd.playerBattlefields.get(player1.getId()).indexOf(flyer))))
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Consulate Skygate cannot attack because it has defender")
    void cannotAttackWithDefender() {
        addReadySkygate(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent addReadySkygate(Player player) {
        Permanent skygate = new Permanent(new ConsulateSkygate());
        skygate.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(skygate);
        return skygate;
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        creature.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
