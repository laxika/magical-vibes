package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HostilityTest extends BaseCardTest {

    @Test
    @DisplayName("A spell you control that would damage an opponent is prevented; you get a token per damage")
    void preventsSpellDamageToOpponentAndCreatesTokens() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Hostility());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // The 3 damage to the opponent is prevented...
        harness.assertLife(player2, 20);
        // ...and Hostility's controller gets three 3/1 Elemental Shaman tokens.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Elemental Shaman"))
                .hasSize(3);
    }

    @Test
    @DisplayName("A spell an opponent controls is not affected: Hostility's controller still takes the damage")
    void doesNotPreventOpponentSpellDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Hostility());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Not "a spell you control" — damage goes through and no tokens are made.
        harness.assertLife(player1, 17);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elemental Shaman"));
    }

    @Test
    @DisplayName("When Hostility is put into the graveyard, a triggered ability shuffles it into its library")
    void diesThenTriggerShufflesIntoLibrary() {
        harness.setLibrary(player1, new java.util.ArrayList<>());
        Permanent hostility = harness.addToBattlefieldAndReturn(player1, new Hostility());
        hostility.setMarkedDamage(6);

        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hostility"));
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Hostility");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hostility"));
    }
}
