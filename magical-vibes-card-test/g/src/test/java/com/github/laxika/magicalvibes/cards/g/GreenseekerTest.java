package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Greenseeker.class, Forest.class, Mountain.class, AshcoatBear.class, TerramorphicExpanse.class})
class GreenseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating starts a discard-cost choice")
    void activationStartsDiscardChoice() {
        addReadyGreenseeker(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discarding a card searches a basic land into hand")
    void discardingSearchesBasicLandIntoHand() {
        addReadyGreenseeker(player1);
        Mountain discarded = new Mountain();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new AshcoatBear()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));

        Card chosen = search.params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(chosen.getId());
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(findPermanent(player1, "Greenseeker").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyGreenseeker(player1);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Finishes without a choice when the library has no basic land")
    void noBasicLandCanBeFound() {
        addReadyGreenseeker(player1);
        Mountain discarded = new Mountain();
        AshcoatBear onlyLibraryCard = new AshcoatBear();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(onlyLibraryCard));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyLibraryCard);
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(findPermanent(player1, "Greenseeker").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can discard a creature and finds only basic lands, revealing the selected land")
    void discardsCreatureAndRevealsBasicLand() {
        addReadyGreenseeker(player1);
        AshcoatBear discarded = new AshcoatBear();
        Forest basic = new Forest();
        TerramorphicExpanse nonbasic = new TerramorphicExpanse();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(nonbasic, basic));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(findPermanent(player1, "Greenseeker").isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(basic);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(basic);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasic);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(basic);
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .anyMatch(text -> text.contains("reveals Forest") && text.contains("Library is shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can fail to find even when a basic land is present")
    void canFailToFindWithBasicLandPresent() {
        addReadyGreenseeker(player1);
        Forest basic = new Forest();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(basic));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basic);
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .anyMatch(text -> text.contains("chooses not to take a card") && text.contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Can activate with an empty library and still pays all costs")
    void emptyLibraryStillPaysCosts() {
        addReadyGreenseeker(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(findPermanent(player1, "Greenseeker").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new Greenseeker());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Mountain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        addReadyGreenseeker(player1);
        findPermanent(player1, "Greenseeker").tap();
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Mountain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the green mana cost with red mana")
    void cannotActivateWithoutGreenMana() {
        addReadyGreenseeker(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Mountain");
        assertThat(findPermanent(player1, "Greenseeker").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addReadyGreenseeker(Player player) {
        addCreatureReady(player, new Greenseeker());
    }
}
