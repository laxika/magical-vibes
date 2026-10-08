package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WreckingCrew.class, CloudSprite.class, GrizzlyBears.class})
class WreckingCrewTest extends BaseCardTest {

    @Test
    @DisplayName("Wrecking Crew can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent attacker = addReady(player1, new CloudSprite());
        attacker.setAttacking(true);
        Permanent blocker = addReady(player2, new WreckingCrew());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cloud Sprite");
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Wrecking Crew assigns excess combat damage to the defending player")
    void trampleDealsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addReady(player1, new WreckingCrew());
        attacker.setAttacking(true);
        Permanent blocker = addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));

        harness.assertLife(player2, 18);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Trample does not deal player damage when the blocker survives all assigned damage")
    void noExcessDamageAgainstLargerBlocker() {
        harness.setLife(player2, 20);
        Permanent attacker = addReady(player1, new WreckingCrew());
        attacker.setAttacking(true);
        Permanent blocker = addReady(player2, new WreckingCrew());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Wrecking Crew");
        harness.assertOnBattlefield(player2, "Wrecking Crew");
        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Trample permits assigning all damage to a blocker instead of the defending player")
    void canAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent attacker = addReady(player1, new WreckingCrew());
        attacker.setAttacking(true);
        Permanent blocker = addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Wrecking Crew");
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
