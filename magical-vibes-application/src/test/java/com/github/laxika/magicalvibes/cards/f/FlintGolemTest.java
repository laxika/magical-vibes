package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlintGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked mills three cards from the defending player's library")
    void blockedMillsDefendingPlayer() {
        addAttackingFlintGolem(player1, player2);
        harness.addToBattlefield(player2, new GrizzlyBears());
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
    @DisplayName("Unblocked Flint Golem does not mill the defending player")
    void unblockedDoesNotMill() {
        addAttackingFlintGolem(player1, player2);
        harness.setLibrary(player2, library(5));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addAttackingFlintGolem(Player attacker, Player defender) {
        Permanent perm = new Permanent(new FlintGolem());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(attacker.getId()).add(perm);
        return perm;
    }

    private List<Card> library(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

}
