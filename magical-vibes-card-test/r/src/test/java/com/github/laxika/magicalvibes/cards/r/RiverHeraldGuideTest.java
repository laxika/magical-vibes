package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiverHeraldGuide.class, Forest.class, Abrade.class})
class RiverHeraldGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Exploring a land puts it into its controller's hand")
    void exploringLandPutsItIntoHand() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        castRiverHeraldGuide();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(findPermanent(player1, "River Herald Guide").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Exploring a nonland puts a +1/+1 counter on River Herald Guide")
    void exploringNonlandPutsCounterOnGuide() {
        Card nonland = new RiverHeraldGuide();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castRiverHeraldGuide();

        assertThat(findPermanent(player1, "River Herald Guide").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining the nonland graveyard choice leaves the card on top")
    void decliningNonlandGraveyardChoiceLeavesCardOnTop() {
        Card nonland = new RiverHeraldGuide();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castRiverHeraldGuide();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(nonland.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(nonland.getId()));
    }

    @Test
    @DisplayName("Accepting the nonland graveyard choice puts the card into the graveyard")
    void acceptingNonlandGraveyardChoicePutsCardIntoGraveyard() {
        Card nonland = new RiverHeraldGuide();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castRiverHeraldGuide();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(nonland.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(nonland.getId()));
    }

    @Test
    @DisplayName("An empty library still gives River Herald Guide a counter")
    void exploringEmptyLibraryPutsCounterOnGuide() {
        harness.setLibrary(player1, List.of());

        castRiverHeraldGuide();

        assertThat(findPermanent(player1, "River Herald Guide")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("River Herald Guide attacks without tapping")
    void attacksWithoutTapping() {
        var guide = addCreatureReady(player1, new RiverHeraldGuide());

        declareAttackers(List.of(0));

        assertThat(guide.isTapped()).isFalse();
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The explore trigger still puts a land into hand after the Guide dies")
    void exploresAfterSourceDies() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new RiverHeraldGuide()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, 0,
                harness.getPermanentId(player1, "River Herald Guide"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "River Herald Guide");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castRiverHeraldGuide() {
        harness.setHand(player1, List.of(new RiverHeraldGuide()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
