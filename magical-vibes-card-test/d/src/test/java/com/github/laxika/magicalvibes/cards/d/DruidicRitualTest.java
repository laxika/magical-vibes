package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Owlbear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DruidicRitual.class, Forest.class, Owlbear.class})
class DruidicRitualTest extends BaseCardTest {

    @Test
    void millsThreeAndReturnsUpToOneCreatureAndLand() {
        Card creature = new Owlbear();
        Card land = new Forest();
        List<Card> milled = List.of(new DruidicRitual(), new DruidicRitual(), new DruidicRitual());
        Card spell = new DruidicRitual();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setLibrary(player1, milled);

        harness.castFromHand(player1, spell, "{2}{G}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(creature));
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(land));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(
                List.of(spell, milled.get(0), milled.get(1), milled.get(2)));
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
    }

    @Test
    void mayDeclineToMillAndSkipReturns() {
        Card creature = new Owlbear();
        Card spell = new DruidicRitual();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new DruidicRitual(), new DruidicRitual(), new DruidicRitual()));

        harness.castFromHand(player1, spell, "{2}{G}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canReturnNewlyMilledCreatureAndLandFromInitiallyEmptyGraveyard() {
        Card creature = new Owlbear();
        Card land = new Forest();
        Card other = new DruidicRitual();
        Card spell = new DruidicRitual();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(creature, land, other));

        harness.castFromHand(player1, spell, "{2}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(creature));
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(land));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(other, spell);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningMillStillAllowsReturningOnlyCreature() {
        Card creature = new Owlbear();
        Card land = new Forest();
        Card libraryCard = new DruidicRitual();
        Card spell = new DruidicRitual();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.castFromHand(player1, spell, "{2}{G}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(creature));
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(land, spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }
}
