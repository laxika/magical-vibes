package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RovingKeep;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.m.MatterReshaper;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CovetousUrge.class, Divination.class, GrizzlyBears.class, RovingKeep.class, Swamp.class,
        MatterReshaper.class})
class CovetousUrgeTest extends BaseCardTest {

    @Test
    void choosesANonlandFromTheRevealedHandOrGraveyard() {
        Card land = new Swamp();
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new Divination();
        harness.setHand(player2, List.of(land, handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));

        castCovetousUrge();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(land.getId())))
                .hasMessageContaining("valid nonland card");

        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(graveyardCard.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, handCard);
    }

    @Test
    void grantsPersistentAnyColorManaCastPermission() {
        Card exiledCard = new Divination();
        harness.setHand(player2, List.of(new Swamp()));
        harness.setGraveyard(player2, List.of(exiledCard));

        castCovetousUrge();
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThat(gd.exilePlayPermissionHolder(exiledCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayAnyManaTypeCardIds()).contains(exiledCard.getId());

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Divination");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(exiledCard.getId()));
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new CovetousUrge()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }

    @Test
    void choosesFromHandAndCastsThePermanentUnderYourControl() {
        Card chosen = new RovingKeep();
        Card land = new Swamp();
        harness.setHand(player2, List.of(chosen, land));

        castCovetousUrge();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .hasMessageContaining("exactly one");
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);

        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Roving Keep");
        harness.assertNotOnBattlefield(player2, "Roving Keep");
        assertThat(gd.exilePlayPermissionHolders()).doesNotContainKey(chosen.getId());
        assertThat(gd.exilePlayAnyManaTypeCardIds()).doesNotContain(chosen.getId());
    }

    @Test
    void resolvesWithoutAChoiceWhenBothZonesContainOnlyLands() {
        Card handLand = new Swamp();
        Card graveyardLand = new Swamp();
        harness.setHand(player2, List.of(handLand));
        harness.setGraveyard(player2, List.of(graveyardLand));

        castCovetousUrge();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardLand);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Covetous Urge");
    }

    private void castCovetousUrge() {
        harness.setHand(player1, List.of(new CovetousUrge()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Mana spent as though it were any color can't pay the {C} symbol of the exiled card")
    void anyColorManaDoesNotPayColorlessSymbol() {
        Card reshaper = new MatterReshaper();
        harness.setHand(player2, List.of(new Swamp()));
        harness.setGraveyard(player2, List.of(reshaper));
        castCovetousUrge();
        harness.handleMultipleCardsChosen(player1, List.of(reshaper.getId()));

        harness.addMana(player1, ManaColor.GREEN, 3);
        assertThatThrownBy(() -> harness.castFromExile(player1, reshaper.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(reshaper);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, reshaper.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Matter Reshaper");
    }

    @Test
    @DisplayName("The exiled card with a {C} symbol isn't shown castable with colored mana only")
    void viewDoesNotOfferColorlessSymbolToAnyColorMana() {
        Card reshaper = new MatterReshaper();
        harness.setHand(player2, List.of(new Swamp()));
        harness.setGraveyard(player2, List.of(reshaper));
        castCovetousUrge();
        harness.handleMultipleCardsChosen(player1, List.of(reshaper.getId()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThat(playableExileCardsShown()).doesNotContain(reshaper.getId().toString());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThat(playableExileCardsShown()).contains(reshaper.getId().toString());
    }

    private String playableExileCardsShown() {
        harness.clearMessages();
        harness.publishState();
        JsonMapper mapper = JsonMapper.builder().build();
        return harness.getConn1().getSentMessages().stream()
                .map(message -> mapper.readTree(message).get("playableExileCards"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .reduce("", String::concat);
    }
}
