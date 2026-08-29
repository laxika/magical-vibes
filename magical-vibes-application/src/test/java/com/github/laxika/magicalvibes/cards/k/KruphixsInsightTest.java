package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BanishingLight;
import com.github.laxika.magicalvibes.cards.d.DictateOfKruphix;
import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.cards.f.FontOfFortunes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void castKruphixsInsight() {
        harness.setHand(player1, List.of(new KruphixsInsight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setTopCards(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
