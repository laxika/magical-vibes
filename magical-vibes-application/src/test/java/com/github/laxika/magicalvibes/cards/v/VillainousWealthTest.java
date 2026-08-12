package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VillainousWealthTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top X cards of the target opponent's library")
    void exilesTopXCardsOfTargetOpponentLibrary() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(first, second, third, fourth));

        cast(3, player2.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(fourth);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Offers nonland spells with mana value X or less from the exiled cards")
    void offersCastableSpellsAtOrBelowX() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Cancel cancel = new Cancel();
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(shock, bears, cancel, forest));

        cast(3, player2.getId());

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactlyInAnyOrder(
                shock.getId(), bears.getId(), cancel.getId());
        assertThat(interaction.validCardIds()).doesNotContain(forest.getId());
    }

    @Test
    @DisplayName("Chosen exiled card is cast for free and unchosen cards remain exiled")
    void castsChosenCardForFreeAndLeavesUnchosenCardsExiled() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(bears, forest));

        cast(2, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("An opponent is required as the target")
    void requiresOpponentTarget() {
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new VillainousWealth()));
        addMana(1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int xValue, java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new VillainousWealth()));
        addMana(xValue);
        harness.castSorcery(player1, 0, xValue, targetPlayerId);
        harness.passBothPriorities();
    }

    private void addMana(int xValue) {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
