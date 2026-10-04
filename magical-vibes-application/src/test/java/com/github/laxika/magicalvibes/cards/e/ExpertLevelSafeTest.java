package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExpertLevelSafe.class, GrizzlyBears.class})
class ExpertLevelSafeTest extends BaseCardTest {

    @Test
    void entersByExilingTopTwoCardsFaceDownWithIt() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Permanent safe = castSafe(List.of(first, second));

        assertThat(gd.getCardsExiledByPermanent(safe.getId())).containsExactly(first, second);
        assertThat(gd.getExiledWithPermanentEntries(safe.getId(), safe.getCard().getId()))
                .allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    void matchingChoicesSacrificeItAndReturnAllCardsExiledWithIt() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Permanent safe = castSafe(List.of(first, second));
        addManaForAbility();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(safe);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(safe.getCard());
        assertThat(gd.getCardsExiledByPermanent(safe.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void nonmatchingChoicesExileOneMoreTopCardFaceDown() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Permanent safe = castSafe(List.of(first, second, third));
        addManaForAbility();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(safe);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(safe.getId())).containsExactly(first, second, third);
        assertThat(gd.getExiledWithPermanentEntries(safe.getId(), safe.getCard().getId()))
                .allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    void activatedAbilityCannotTargetItsController() {
        harness.addToBattlefieldAndReturn(player1, new ExpertLevelSafe());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void entersWithOnlyOneCardInLibrary() {
        Card onlyCard = new ExpertLevelSafe();
        Permanent safe = castSafe(List.of(onlyCard));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(safe.getId())).containsExactly(onlyCard);
        assertThat(gd.getExiledWithPermanentEntries(safe.getId(), safe.getCard().getId()))
                .allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    void nonmatchingChoicesWithEmptyLibraryLeaveSafeOnBattlefield() {
        Permanent safe = castSafe(List.of());
        addManaForAbility();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(safe);
        assertThat(gd.getCardsExiledByPermanent(safe.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void matchingChoicesReturnCardsAccumulatedByEarlierNonmatch() {
        Card first = new ExpertLevelSafe();
        Card second = new ExpertLevelSafe();
        Card third = new ExpertLevelSafe();
        Permanent safe = castSafe(List.of(first, second, third));
        addManaForAbility();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 2);

        safe.untap();
        addManaForAbility();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player2, 3);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(safe);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.getCardsExiledByPermanent(safe.getId())).isEmpty();
    }

    @Test
    void choicesOutsideOneThroughThreeAreRejected() {
        Permanent safe = castSafe(List.of());
        addManaForAbility();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 4))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleXValueChosen(player1, 1);
        assertThatThrownBy(() -> harness.handleXValueChosen(player2, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> harness.handleXValueChosen(player2, 4))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(safe);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(safe.getCard());
    }

    @Test
    void faceDownExiledCardsAreHiddenFromBothPlayers() throws Exception {
        Permanent safe = castSafe(List.of(new ExpertLevelSafe(), new ExpertLevelSafe()));
        harness.publishState();

        for (var connection : List.of(harness.getConn1(), harness.getConn2())) {
            String message = connection.getMessagesContaining("\"type\":\"GAME_STATE\"").getLast();
            GameStateMessage state = new JacksonConfig().objectMapper().readValue(message, GameStateMessage.class);
            var safeView = state.battlefields().stream().flatMap(List::stream)
                    .filter(permanent -> permanent.id().equals(safe.getId())).findFirst().orElseThrow();
            assertThat(safeView.faceDownExiledCards()).isEmpty();
            assertThat(safeView.faceDownExiledCount()).isEqualTo(2);
        }
    }

    private Permanent castSafe(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new ExpertLevelSafe(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
