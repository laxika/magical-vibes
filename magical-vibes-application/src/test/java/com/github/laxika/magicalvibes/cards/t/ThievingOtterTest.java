package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.ViridianLongbow;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ThievingOtter.class, Forest.class, GrizzlyBears.class, ViridianLongbow.class})
class ThievingOtterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it deals combat damage to an opponent")
    void drawsOnCombatDamageToOpponent() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent otter = addAttacker(player1, new ThievingOtter());

        resolveUnblockedCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(otter.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Does not draw when blocked and no damage reaches the opponent")
    void doesNotDrawWhenBlocked() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent otter = addAttacker(player1, new ThievingOtter());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(otter))));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws a card when it deals noncombat damage to an opponent")
    void drawsOnNoncombatDamageToOpponent() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent otter = addReadyCreature(player1, new ThievingOtter());
        Permanent longbow = addReadyCreature(player1, new ViridianLongbow());
        longbow.setAttachedTo(otter.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addAttacker(Player player, Card card) {
        Permanent attacker = addReadyCreature(player, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void resolveUnblockedCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
