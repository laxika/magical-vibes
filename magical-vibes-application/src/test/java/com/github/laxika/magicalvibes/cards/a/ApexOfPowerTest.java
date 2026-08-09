package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApexOfPowerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles seven cards, permits nonlands to be cast, and adds ten chosen-color mana from hand")
    void exilesSevenAndAddsManaWhenCastFromHand() {
        Card land = createLand("Exiled Land");
        List<Card> topCards = List.of(
                createSpell("Spell One"), land, createSpell("Spell Three"),
                createSpell("Spell Four"), createSpell("Spell Five"),
                createSpell("Spell Six"), createSpell("Spell Seven"));
        harness.setLibrary(player1, topCards);
        harness.setHand(player1, List.of(new ApexOfPower()));
        addApexMana(player1);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyElementsOf(topCards.stream().map(Card::getId).toList());
        assertThat(gd.exilePlayPermissions).containsKeys(
                topCards.get(0).getId(), topCards.get(2).getId(), topCards.get(3).getId(),
                topCards.get(4).getId(), topCards.get(5).getId(), topCards.get(6).getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(10);
    }

    @Test
    @DisplayName("Does not add mana when cast from exile")
    void doesNotAddManaWhenCastFromExile() {
        ApexOfPower apex = new ApexOfPower();
        harness.setExile(player1, List.of(apex));
        gd.exilePlayPermissions.put(apex.getId(), player1.getId());
        harness.setLibrary(player1, List.of(
                createSpell("Spell One"), createSpell("Spell Two"), createSpell("Spell Three"),
                createSpell("Spell Four"), createSpell("Spell Five"), createSpell("Spell Six"),
                createSpell("Spell Seven")));
        addApexMana(player1);

        harness.castFromExile(player1, apex.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private void addApexMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 7);
        harness.addMana(player, ManaColor.RED, 3);
    }

    private Card createSpell(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        return card;
    }

    private Card createLand(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        return card;
    }
}
