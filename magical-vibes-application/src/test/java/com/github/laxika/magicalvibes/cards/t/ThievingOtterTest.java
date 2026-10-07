package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.ViridianLongbow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
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
        Permanent otter = addCreatureReady(player1, new ThievingOtter());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        longbow.setAttachedTo(otter.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when it deals damage to its own controller")
    void doesNotDrawOnDamageToController() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent otter = addCreatureReady(player1, new ThievingOtter());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        longbow.setAttachedTo(otter.getId());

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when it deals noncombat damage to a creature")
    void doesNotDrawOnNoncombatDamageToCreature() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent otter = addCreatureReady(player1, new ThievingOtter());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        longbow.setAttachedTo(otter.getId());
        Permanent target = addCreatureReady(player2, new ThievingOtter());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addAttacker(Player player, Card card) {
        Permanent attacker = addCreatureReady(player, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private void resolveUnblockedCombat() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
