package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MoggToady;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootwaterThief.class, MoggToady.class})
class RootwaterThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent thief = addCreatureReady(player1, new RootwaterThief());
        Permanent otherThief = addCreatureReady(player1, new RootwaterThief());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(thief.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(otherThief.hasKeyword(Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thief.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Combat damage trigger can pay to exile a card from the damaged player's library")
    void paysToExileCardFromDamagedPlayersLibrary() {
        Permanent thief = addCreatureReady(player1, new RootwaterThief());
        thief.setAttacking(true);
        Card exiledCard = new MoggToady();
        harness.setLibrary(player2, List.of(exiledCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting("id").contains(exiledCard.getId());
        assertThat(gd.findExiledCard(exiledCard.getId()).faceDown()).isFalse();
    }

    @Test
    @DisplayName("Combat damage trigger does nothing when its optional payment is declined")
    void declinesPayment() {
        Permanent thief = addCreatureReady(player1, new RootwaterThief());
        thief.setAttacking(true);
        Card libraryCard = new MoggToady();
        harness.setLibrary(player2, List.of(libraryCard));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(card -> card.getId().equals(libraryCard.getId()));
    }

    @Test
    @DisplayName("Combat damage trigger does not fire when the creature is blocked")
    void doesNotTriggerWhenBlocked() {
        Permanent thief = addCreatureReady(player1, new RootwaterThief());
        thief.setAttacking(true);
        addCreatureReady(player2, new RootwaterThief());
        Card libraryCard = new MoggToady();
        harness.setLibrary(player2, List.of(libraryCard));

        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(thief);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex)));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
    }
}
