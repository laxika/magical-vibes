package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.p.PincherBeetles;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvenWarhounds.class, HornedTurtle.class, LowlandGiant.class, PincherBeetles.class})
class ElvenWarhoundsTest extends BaseCardTest {

    @Test
    @DisplayName("The blocking creature is put on top of its owner's library")
    void tucksBlocker() {
        addAttackingWarhounds();
        Permanent blocker = addBlocker(new HornedTurtle());

        block();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(blocker.getCard());
    }

    @Test
    @DisplayName("The blocking creature goes to its owner's library even when another player controls it")
    void tucksBlockerIntoOwnersLibrary() {
        addAttackingWarhounds();
        Card blockerCard = new LowlandGiant();
        blockerCard.setOwnerId(player1.getId());
        Permanent blocker = addBlocker(blockerCard);

        block();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(blocker.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(blocker.getCard());
    }

    @Test
    @DisplayName("Every blocker is tucked when multiple creatures block")
    void tucksEveryBlocker() {
        addAttackingWarhounds();
        Permanent turtle = addBlocker(new HornedTurtle());
        Permanent giant = addBlocker(new LowlandGiant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).subList(0, 2))
                .containsExactlyInAnyOrder(turtle.getCard(), giant.getCard());
    }

    @Test
    @DisplayName("Nothing is tucked when the Warhounds attack unblocked")
    void unblockedTucksNothing() {
        addAttackingWarhounds();
        Permanent bystander = addBlocker(new HornedTurtle());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bystander);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(bystander.getCard());
    }

    @Test
    @DisplayName("Shroud does not prevent tucking a blocking creature")
    void tucksBlockerWithShroud() {
        addAttackingWarhounds();
        Permanent blocker = addBlocker(new PincherBeetles());

        block();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(blocker.getCard());
    }

    @Test
    @DisplayName("Warhounds remain blocked after tucking their blocker")
    void tuckedBlockerDoesNotLetCombatDamageThrough() {
        harness.setLife(player2, 20);
        addAttackingWarhounds();
        Permanent blocker = addBlocker(new LowlandGiant());

        block();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(blocker.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Blocking with Warhounds does not tuck the attacker")
    void blockingDoesNotTriggerAbility() {
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());
        attacker.setAttacking(true);
        Permanent hounds = addBlocker(new ElvenWarhounds());

        block();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hounds);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(attacker.getCard());
    }
    private void addAttackingWarhounds() {
        Permanent hounds = addCreatureReady(player1, new ElvenWarhounds());
        hounds.setAttacking(true);
    }

    private Permanent addBlocker(Card card) {
        return addCreatureReady(player2, card);
    }

    private void block() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
    }
}
