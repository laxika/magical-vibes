package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EpicExperimentTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top X cards of the library")
    void exilesTopXCards() {
        Forest a = new Forest();
        Forest b = new Forest();
        Forest c = new Forest();
        Forest leftover = new Forest();
        cast(2, List.of(a, b, c, leftover));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(c, leftover);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(a, b);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Offers only exiled instants/sorceries with mana value X or less")
    void onlyOffersInstantsAndSorceriesAtOrBelowX() {
        Shock shock = new Shock();                 // instant, MV 1
        GiantGrowth growth = new GiantGrowth();    // instant, MV 1
        GrizzlyBears bears = new GrizzlyBears();   // creature — not castable
        Forest forest = new Forest();              // land — not castable

        cast(3, List.of(shock, growth, bears, forest));

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactlyInAnyOrder(shock.getId(), growth.getId());
        assertThat(interaction.validCardIds()).doesNotContain(bears.getId(), forest.getId());
    }

    @Test
    @DisplayName("Instant/sorcery with mana value greater than X is not offered")
    void excludesSpellsAboveX() {
        // Cancel is {1}{U}{U} = MV 3; X=2 should not offer it.
        com.github.laxika.magicalvibes.cards.c.Cancel cancel =
                new com.github.laxika.magicalvibes.cards.c.Cancel();
        Shock shock = new Shock();

        cast(2, List.of(cancel, shock));

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactly(shock.getId());
        assertThat(interaction.validCardIds()).doesNotContain(cancel.getId());
    }

    @Test
    @DisplayName("Choosing an exiled instant casts it without paying; unchosen cards go to the graveyard")
    void castsChosenAndGraveyardsRemainder() {
        Shock shock = new Shock();
        Forest forest = new Forest();
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(2, List.of(shock, forest));

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearId);

        assertThat(gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Shock"))).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Declining every cast puts all exiled cards into the graveyard")
    void decliningPutsAllIntoGraveyard() {
        Shock shock = new Shock();
        Forest forest = new Forest();

        cast(2, List.of(shock, forest));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, forest);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    private void cast(int xValue, List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new EpicExperiment()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }
}
