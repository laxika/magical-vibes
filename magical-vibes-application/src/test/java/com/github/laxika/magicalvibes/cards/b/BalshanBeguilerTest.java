package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalshanBeguiler.class, Island.class, Forest.class, WoodlandDruid.class})
class BalshanBeguilerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage reveals the damaged player's top two cards and lets the controller put one into their graveyard")
    void revealsDamagedPlayersTopCardsAndPutsOneIntoGraveyard() {
        Card ownCard = new Island();
        Card opponentTop = new Forest();
        Card opponentSecond = new WoodlandDruid();
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

        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentSecond);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
    }

    @Test
    @DisplayName("A library with only one card reveals and puts that card into the graveyard")
    void oneCardLibraryIsHandled() {
        Card onlyCard = new Forest();
        harness.setLibrary(player2, List.of(onlyCard));
        addAttackingBeguiler();

        resolveCombatAndTrigger();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice).isNotNull();
        assertThat(choice.params().cards()).containsExactly(onlyCard);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(onlyCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A blocked Beguiler does not trigger")
    void blockedDoesNotTrigger() {
        addAttackingBeguiler();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WoodlandDruid());
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
