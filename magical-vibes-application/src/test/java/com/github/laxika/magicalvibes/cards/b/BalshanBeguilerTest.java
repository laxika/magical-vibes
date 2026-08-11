package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BalshanBeguilerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage reveals the damaged player's top two cards and lets the controller put one into their graveyard")
    void revealsDamagedPlayersTopCardsAndPutsOneIntoGraveyard() {
        Card ownCard = new Island();
        Card opponentTop = new Forest();
        Card opponentSecond = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(opponentTop, opponentSecond));
        addAttackingBeguiler();

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals") && log.contains(opponentTop.getName())
                        && log.contains(opponentSecond.getName()));
        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice).isNotNull();
        assertThat(choice.params().playerId()).isEqualTo(player1.getId());
        assertThat(choice.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.params().cards()).containsExactly(opponentTop, opponentSecond);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentSecond);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
    }

    @Test
    @DisplayName("A blocked Beguiler does not trigger")
    void blockedDoesNotTrigger() {
        addAttackingBeguiler();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player2, List.of(new Forest(), new Island()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty damaged player's library produces no choice")
    void emptyLibraryProducesNoChoice() {
        addAttackingBeguiler();
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addAttackingBeguiler() {
        Permanent beguiler = harness.addToBattlefieldAndReturn(player1, new BalshanBeguiler());
        beguiler.setSummoningSick(false);
        beguiler.setAttacking(true);
        return beguiler;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
