package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Fireslinger;
import com.github.laxika.magicalvibes.cards.k.Kindle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JackalPup.class, Kindle.class, Fireslinger.class})
class JackalPupTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage: Jackal Pup deals that much damage to its controller")
    void nonCombatDamageReflectedToController() {
        harness.addToBattlefield(player2, new JackalPup()); // 2/1
        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        UUID pupId = harness.getPermanentId(player2, "Jackal Pup");
        harness.castInstant(player1, 0, pupId);
        harness.passBothPriorities(); // Kindle deals 2, ON_DEALT_DAMAGE queued
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Jackal Pup"); // 2/1 dies to 2 damage
    }

    @Test
    @DisplayName("Combat damage: Jackal Pup's controller takes the combat damage dealt to it")
    void combatDamageReflectedToController() {
        addCreatureReady(player1, new Fireslinger()); // 1/1
        addCreatureReady(player2, new JackalPup()); // 2/1
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1); // Combat damage: Pup takes 1, trigger queued
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Jackal Pup"); // 2/1 dies to 1 damage
    }
}
