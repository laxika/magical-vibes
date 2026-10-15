package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightveilPredator.class, ColossalDreadmaw.class, AirElemental.class, Shock.class})
class NightveilPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent predator = addCreatureReady(player1, new NightveilPredator());
        predator.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ColossalDreadmaw());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(predator);

        assertThatThrownBy(() -> gs.declareBlockers(
                        gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deathtouch destroys a larger creature in combat")
    void deathtouchDestroysLargerCreature() {
        Permanent predator = addCreatureReady(player1, new NightveilPredator());
        predator.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(predator);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Opponent cannot target Nightveil Predator with a spell")
    void opponentCannotTargetWithSpell() {
        Permanent predator = addCreatureReady(player1, new NightveilPredator());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, predator.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target Nightveil Predator with a spell")
    void controllerCanTargetWithSpell() {
        Permanent predator = addCreatureReady(player1, new NightveilPredator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, predator.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(predator);
        assertThat(predator.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Hexproof does not prevent blocking or combat damage")
    void hexproofDoesNotPreventCombat() {
        Permanent attacker = addCreatureReady(player1, new NightveilPredator());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new NightveilPredator());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
