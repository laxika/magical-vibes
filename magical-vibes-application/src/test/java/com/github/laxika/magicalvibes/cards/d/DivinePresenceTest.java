package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DivinePresenceTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces four spell damage to a player with three")
    void replacesSpellDamageToPlayer() {
        harness.addToBattlefield(player1, new DivinePresence());
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 4, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Does not replace three damage")
    void leavesDamageBelowThresholdUnchanged() {
        harness.addToBattlefield(player1, new DivinePresence());
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 3, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Replaces damage to a permanent")
    void replacesDamageToPermanent() {
        harness.addToBattlefield(player1, new DivinePresence());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 4, angel.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(angel);
        assertThat(angel.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Replaces combat damage")
    void replacesCombatDamage() {
        harness.addToBattlefield(player2, new DivinePresence());
        GrizzlyBears attacker = new GrizzlyBears();
        attacker.setPower(5);
        attacker.setToughness(5);
        addCreatureReady(player1, attacker);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        harness.assertLife(player2, 17);
    }
}
