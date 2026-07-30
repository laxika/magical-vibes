package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VengefulPharaohTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to you destroys the attacker and puts Vengeful Pharaoh on top of your library")
    void destroysAttackerAndTucksItself() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new VengefulPharaoh()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player2, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(attacker.getId()));
        harness.assertNotInGraveyard(player2, "Vengeful Pharaoh");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Vengeful Pharaoh");
    }

    @Test
    @DisplayName("No trigger when the attack deals no combat damage to you")
    void doesNotTriggerWithoutCombatDamage() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new VengefulPharaoh()));

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vengeful Pharaoh");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Vengeful Pharaoh"));
    }

    @Test
    @DisplayName("Does not trigger from the attacking player's graveyard")
    void doesNotTriggerFromAttackerGraveyard() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new VengefulPharaoh()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
        harness.assertInGraveyard(player1, "Vengeful Pharaoh");
    }
}
