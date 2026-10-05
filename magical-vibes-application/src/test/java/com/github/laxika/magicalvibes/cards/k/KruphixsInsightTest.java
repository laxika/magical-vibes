package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BanishingLight;
import com.github.laxika.magicalvibes.cards.d.DictateOfKruphix;
import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.cards.f.FontOfFortunes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NyxFleeceRam;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KruphixsInsight.class, BanishingLight.class, DictateOfKruphix.class,
        FontOfFertility.class, FontOfFortunes.class, Forest.class, Shock.class, NyxFleeceRam.class})
class KruphixsInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Puts up to three revealed enchantments into hand and the rest into the graveyard")
    void putsUpToThreeEnchantmentsIntoHand() {
        Card banishingLight = new BanishingLight();
        Card dictateOfKruphix = new DictateOfKruphix();
        Card fontOfFertility = new FontOfFertility();
        Card fontOfFortunes = new FontOfFortunes();
        Card forest = new Forest();
        Card shock = new Shock();
        setTopCards(banishingLight, dictateOfKruphix, fontOfFertility, fontOfFortunes, forest, shock);

        castKruphixsInsight();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1,
                List.of(banishingLight.getId(), dictateOfKruphix.getId(), fontOfFertility.getId()));

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(banishingLight, dictateOfKruphix, fontOfFertility);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(fontOfFortunes, forest, shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Allows fewer than three enchantments to be kept")
    void allowsFewerThanThreeEnchantments() {
        Card banishingLight = new BanishingLight();
        Card dictateOfKruphix = new DictateOfKruphix();
        Card fontOfFertility = new FontOfFertility();
        Card forest = new Forest();
        Card shock = new Shock();
        setTopCards(banishingLight, dictateOfKruphix, fontOfFertility, forest, shock);

        castKruphixsInsight();
        harness.handleMultipleCardsChosen(player1, List.of(banishingLight.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(banishingLight);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(dictateOfKruphix, fontOfFertility, forest, shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Non-enchantment cards cannot be chosen")
    void nonEnchantmentCardsGoToGraveyard() {
        Card forest = new Forest();
        Card shock = new Shock();
        setTopCards(forest, shock);

        castKruphixsInsight();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, shock);
    }

    @Test
    @DisplayName("Allows all enchantments to be declined even when fewer than three are revealed")
    void allowsZeroEnchantments() {
        Card banishingLight = new BanishingLight();
        Card fontOfFertility = new FontOfFertility();
        setTopCards(banishingLight, fontOfFertility);

        castKruphixsInsight();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(banishingLight, fontOfFertility);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reveals only the top six cards and leaves later cards in the library")
    void leavesSeventhCardInLibrary() {
        Card seventh = new FontOfFortunes();
        List<Card> topSix = List.of(new FontOfFertility(), new FontOfFertility(),
                new FontOfFertility(), new FontOfFertility(), new FontOfFertility(), new FontOfFertility());
        harness.setLibrary(player1, List.of(topSix.get(0), topSix.get(1), topSix.get(2),
                topSix.get(3), topSix.get(4), topSix.get(5), seventh));

        castKruphixsInsight();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(seventh.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1,
                List.of(topSix.get(0).getId(), topSix.get(1).getId(), topSix.get(2).getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topSix.subList(0, 3));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(topSix.subList(3, 6));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(seventh);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Rejects more than three enchantments, non-enchantments, and duplicate selections")
    void rejectsInvalidSelectionsWithoutLosingChoice() {
        Card banishingLight = new BanishingLight();
        Card dictateOfKruphix = new DictateOfKruphix();
        Card fontOfFertility = new FontOfFertility();
        Card fontOfFortunes = new FontOfFortunes();
        Card shock = new Shock();
        setTopCards(banishingLight, dictateOfKruphix, fontOfFertility, fontOfFortunes, shock);

        castKruphixsInsight();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(banishingLight.getId(), dictateOfKruphix.getId(),
                        fontOfFertility.getId(), fontOfFortunes.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(shock.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(banishingLight.getId(), banishingLight.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(banishingLight.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(banishingLight);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(dictateOfKruphix, fontOfFertility, fontOfFortunes, shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolves with an empty library without requiring a choice")
    void resolvesWithEmptyLibrary() {
        setTopCards();

        castKruphixsInsight();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Kruphix's Insight");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enchantment creatures can be put into hand without entering the battlefield")
    void acceptsEnchantmentCreatures() {
        Card ram = new NyxFleeceRam();
        setTopCards(ram);

        castKruphixsInsight();
        harness.handleMultipleCardsChosen(player1, List.of(ram.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ram);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ram);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castKruphixsInsight() {
        harness.setHand(player1, List.of(new KruphixsInsight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setTopCards(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
