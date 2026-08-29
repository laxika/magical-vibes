package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("May discard its hand and draw that many cards after dealing combat damage")
    void discardsHandAndDrawsThatManyCardsWhenAccepted() {
        addAttackingBookDevourer();
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining does not discard or draw")
    void decliningDoesNothing() {
        addAttackingBookDevourer();
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger when blocked and no combat damage reaches a player")
    void noTriggerWhenBlocked() {
        Permanent bookDevourer = addAttackingBookDevourer();
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bookDevourer);
    }

    private Permanent addAttackingBookDevourer() {
        Permanent bookDevourer = addCreatureReady(player1, new BookDevourer());
        bookDevourer.setAttacking(true);
        return bookDevourer;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
