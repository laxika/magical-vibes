package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlintGolem.class, Mossdog.class})
class FlintGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked mills three cards from the defending player's library")
    void blockedMillsDefendingPlayer() {
        addAttackingFlintGolem(player1, player2);
        addCreatureReady(player2, new Mossdog());
        harness.setLibrary(player2, library(5));
        int attackerLibrarySize = gd.playerDecks.get(player1.getId()).size();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(attackerLibrarySize);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures mills three cards only once")
    void blockedByMultipleCreaturesMillsOnlyOnce() {
        addAttackingFlintGolem(player1, player2);
        addCreatureReady(player2, new Mossdog());
        addCreatureReady(player2, new Mossdog());
        harness.setLibrary(player2, library(6));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Unblocked Flint Golem does not mill the defending player")
    void unblockedDoesNotMill() {
        addAttackingFlintGolem(player1, player2);
        harness.setLibrary(player2, library(5));

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addAttackingFlintGolem(Player attacker, Player defender) {
        Permanent perm = addCreatureReady(attacker, new FlintGolem());
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private List<Card> library(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Mossdog());
        }
        return cards;
    }

}
