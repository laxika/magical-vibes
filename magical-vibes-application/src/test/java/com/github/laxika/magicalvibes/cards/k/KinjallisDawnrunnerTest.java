package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KinjallisDawnrunner.class, Forest.class, GrizzlyBears.class})
class KinjallisDawnrunnerTest extends BaseCardTest {

    @Test
    @DisplayName("When it explores a land, Kinjalli's Dawnrunner puts it into its controller's hand")
    void exploreLandGoesToHand() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castDawnrunner();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(findDawnrunner().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("When it explores a nonland, Kinjalli's Dawnrunner gets a +1/+1 counter and prompts")
    void exploreNonlandAddsCounterAndPrompts() {
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        castDawnrunner();

        assertThat(findDawnrunner().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Accepting the nonland explore choice puts the card into the graveyard")
    void exploreNonlandAcceptPutsCardInGraveyard() {
        Card nonland = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castDawnrunner();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(nonland.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(nonland.getId()));
    }

    @Test
    @DisplayName("Declining the nonland explore choice leaves the card on top of the library")
    void exploreNonlandDeclineLeavesCardOnTop() {
        Card nonland = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castDawnrunner();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(nonland.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(nonland.getId()));
    }

    @Test
    @DisplayName("Exploring with an empty library does nothing")
    void exploreEmptyLibrary() {
        gd.playerDecks.get(player1.getId()).clear();

        castDawnrunner();

        assertThat(findDawnrunner().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void castDawnrunner() {
        harness.setHand(player1, List.of(new KinjallisDawnrunner()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findDawnrunner() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Kinjalli's Dawnrunner"))
                .findFirst()
                .orElseThrow();
    }
}
