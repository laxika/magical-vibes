package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExpeditionLookout.class, GrizzlyBears.class, Spellbook.class})
class ExpeditionLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack while no opponent has eight cards in their graveyard")
    void cannotAttackBelowThreshold() {
        Permanent lookout = addCreatureReady(player1, new ExpeditionLookout());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(lookout.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack and cannot be blocked when an opponent has eight cards in their graveyard")
    void attacksUnblockablyAtThreshold() {
        harness.setGraveyard(player2, graveyardOfSize(8));
        addCreatureReady(player1, new ExpeditionLookout());

        declareAttackers(List.of(0));
    }

    @Test
    @DisplayName("Cannot be blocked when an opponent has eight cards in their graveyard")
    void cannotBeBlockedAtThreshold() {
        harness.setGraveyard(player2, graveyardOfSize(8));
        Permanent lookout = addCreatureReady(player1, new ExpeditionLookout());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        lookout.setAttacking(true);

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(lookout)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The controller's graveyard does not enable the ability")
    void ownGraveyardDoesNotEnableAbility() {
        harness.setGraveyard(player1, graveyardOfSize(8));
        Permanent lookout = addCreatureReady(player1, new ExpeditionLookout());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(lookout.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can be blocked below the opponent graveyard threshold")
    void canBeBlockedBelowThreshold() {
        harness.setGraveyard(player2, graveyardOfSize(7));
        Permanent lookout = addCreatureReady(player1, new ExpeditionLookout());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        lookout.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(lookout))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private List<Card> graveyardOfSize(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Spellbook());
        }
        return cards;
    }
}
