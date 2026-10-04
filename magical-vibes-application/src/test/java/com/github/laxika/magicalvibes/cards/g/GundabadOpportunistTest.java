package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GundabadOpportunist.class, Forest.class, Shock.class})
class GundabadOpportunistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles the top card with permission to play it until the end of your next turn")
    void etbExilesTopCardWithNextTurnPlayPermission() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new GundabadOpportunist(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    @DisplayName("A card exiled by the ETB can be played from exile for its normal cost")
    void exiledCardCanBePlayedFromExile() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromHand(player1, new GundabadOpportunist(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An exiled land can be played using the ordinary land play")
    void exiledLandCanBePlayed() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.castFromHand(player1, new GundabadOpportunist(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromExile(player1, land.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(land.getId()));
        harness.setHand(player1, List.of(new Forest()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Permission does not waive the exiled spell's mana cost")
    void exiledSpellRequiresMana() {
        Card spell = new Shock();
        harness.setLibrary(player1, List.of(spell));
        harness.castFromHand(player1, new GundabadOpportunist(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An empty library causes the enter trigger to do nothing")
    void emptyLibraryExilesNothing() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new GundabadOpportunist(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GundabadOpportunist);
    }

    @Test
    @DisplayName("The exiled instant remains castable during your next end step")
    void exiledInstantCanBeCastDuringNextEndStep() {
        Card spell = new Shock();
        harness.setLibrary(player1, List.of(spell, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new GundabadOpportunist(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
