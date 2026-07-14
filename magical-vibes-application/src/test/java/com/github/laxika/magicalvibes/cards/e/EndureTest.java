package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EndureTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Endure prevents all damage to the controller")
    void preventsDamageToController() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Endure()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        // Now burn the protected player.
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.s.Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Resolving Endure prevents damage to a permanent the controller controls")
    void preventsDamageToControlledPermanent() {
        harness.setHand(player2, List.of(new Endure()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(creature);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.s.Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Grizzly Bears (2/2) takes 2 damage from Shock, but it is prevented.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Endure does not protect the opponent")
    void doesNotProtectOpponent() {
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Endure()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.s.Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }
}
