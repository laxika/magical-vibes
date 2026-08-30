package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuspiciousStowaway.class, GrizzlyBears.class})
class SuspiciousStowawayTest extends BaseCardTest {

    @Test
    @DisplayName("Suspicious Stowaway cannot be blocked")
    void cannotBeBlocked() {
        Permanent stowaway = addCreatureReady(player1, new SuspiciousStowaway());
        stowaway.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(stowaway)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Suspicious Stowaway draws then discards after dealing combat damage")
    void drawsThenDiscardsAfterCombatDamage() {
        Permanent stowaway = addCreatureReady(player1, new SuspiciousStowaway());
        stowaway.setAttacking(true);
        GrizzlyBears kept = new GrizzlyBears();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        setDeck(player1, List.of(kept));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Transforms to Seafaring Werewolf when no spells were cast last turn")
    void transformsToBackWhenNoSpellsWereCast() {
        Permanent stowaway = addCreatureReady(player1, new SuspiciousStowaway());
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolve(player1);

        assertThat(stowaway.isTransformed()).isTrue();
        assertThat(stowaway.getCard()).isInstanceOf(SeafaringWerewolf.class);
    }

    @Test
    @DisplayName("Seafaring Werewolf draws a card after dealing combat damage")
    void backFaceDrawsAfterCombatDamage() {
        Permanent stowaway = addCreatureReady(player1, new SuspiciousStowaway());
        transformToBack(stowaway);
        stowaway.setAttacking(true);
        GrizzlyBears drawn = new GrizzlyBears();
        setDeck(player1, List.of(drawn));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Seafaring Werewolf transforms back when a player cast two spells last turn")
    void transformsBackWhenTwoSpellsWereCast() {
        Permanent stowaway = addCreatureReady(player1, new SuspiciousStowaway());
        transformToBack(stowaway);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeepAndResolve(player2);

        assertThat(stowaway.isTransformed()).isFalse();
        assertThat(stowaway.getCard()).isInstanceOf(SuspiciousStowaway.class);
    }

    private void transformToBack(Permanent stowaway) {
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1);
        assertThat(stowaway.isTransformed()).isTrue();
    }

    private void advanceToUpkeepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
