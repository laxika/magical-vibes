package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RootwaterThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent thief = addReadyThief(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(thief.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thief.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Combat damage trigger can pay to exile a card from the damaged player's library")
    void paysToExileCardFromDamagedPlayersLibrary() {
        Permanent thief = addReadyThief(player1);
        thief.setAttacking(true);
        Card exiledCard = new Shock();
        harness.setLibrary(player2, List.of(exiledCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting("id").contains(exiledCard.getId());
        assertThat(gd.findExiledCard(exiledCard.getId()).faceDown()).isTrue();
    }

    @Test
    @DisplayName("Combat damage trigger does nothing when its optional payment is declined")
    void declinesPayment() {
        Permanent thief = addReadyThief(player1);
        thief.setAttacking(true);
        Card libraryCard = new Shock();
        harness.setLibrary(player2, List.of(libraryCard));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(card -> card.getId().equals(libraryCard.getId()));
    }

    private Permanent addReadyThief(Player player) {
        Permanent thief = new Permanent(new RootwaterThief());
        thief.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(thief);
        return thief;
    }
}
