package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NobleTemplar.class, Plains.class, Forest.class})
class NobleTemplarTest extends BaseCardTest {

    @Test
    @DisplayName("Plainscycling discards Noble Templar and searches for a Plains")
    void plainscyclingSearchesForPlains() {
        NobleTemplar templar = new NobleTemplar();
        Card plains = new Plains();
        Card forest = new Forest();
        harness.setHand(player1, List.of(templar));
        harness.setLibrary(player1, List.of(plains, forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plains);
        assertThat(search.params().reveals()).isTrue();
        harness.assertInGraveyard(player1, "Noble Templar");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Plainscycling completes without a choice when no Plains is in the library")
    void plainscyclingCanFailToFind() {
        NobleTemplar templar = new NobleTemplar();
        Card forest = new Forest();
        harness.setHand(player1, List.of(templar));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Noble Templar");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Plainscycling requires two generic mana")
    void plainscyclingRequiresTwoGenericMana() {
        NobleTemplar templar = new NobleTemplar();
        harness.setHand(player1, List.of(templar));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(templar);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Vigilance lets Noble Templar attack without tapping")
    void vigilanceKeepsNobleTemplarUntappedWhenAttacking() {
        Permanent templar = addCreatureReady(player1, new NobleTemplar());

        declareAttackers(List.of(0));

        assertThat(templar.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Plainscycling can decline to find an available Plains")
    void plainscyclingCanDeclineAvailablePlains() {
        Card plains = new Plains();
        harness.setHand(player1, List.of(new NobleTemplar()));
        harness.setLibrary(player1, List.of(plains));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        harness.assertInGraveyard(player1, "Noble Templar");
    }

    @Test
    @DisplayName("Plainscycling discards as a cost and does not draw a card")
    void plainscyclingDiscardsBeforeResolvingWithoutDrawing() {
        Card templar = new NobleTemplar();
        Card forest = new Forest();
        harness.setHand(player1, List.of(templar));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(templar);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Plainscycling resolves with an empty library")
    void plainscyclingWithEmptyLibrary() {
        harness.setHand(player1, List.of(new NobleTemplar()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Noble Templar");
    }
}
