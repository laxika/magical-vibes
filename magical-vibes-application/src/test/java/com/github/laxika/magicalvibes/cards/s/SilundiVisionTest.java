package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FieldResearch;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.m.MerfolkWindrobber;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilundiVision.class, SilundiIsle.class, FieldResearch.class, Forest.class, MerfolkWindrobber.class, IntoTheRoil.class})
class SilundiVisionTest extends BaseCardTest {

    @Test
    void visionRevealsAnInstantOrSorceryAndBottomsTheRestRandomly() {
        IntoTheRoil instant = new IntoTheRoil();
        MerfolkWindrobber creature = new MerfolkWindrobber();
        Forest land = new Forest();
        FieldResearch sorcery = new FieldResearch();
        harness.setLibrary(player1, List.of(instant, creature, land, sorcery));
        harness.castFromHand(player1, new SilundiVision(), "{2}{U}");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactlyInAnyOrder(instant, creature, land, sorcery);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(instant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land, sorcery);
    }

    @Test
    void isleCanBePlayedAsATappedBlueManaSource() {
        SilundiVision card = new SilundiVision();
        harness.setHand(player1, List.of(card));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent isle = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(isle.getCard()).isInstanceOf(SilundiIsle.class);
        assertThat(isle.isTapped()).isTrue();

        isle.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void mayDeclineTheOnlyEligibleCard() {
        IntoTheRoil instant = new IntoTheRoil();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(instant, land));
        harness.castFromHand(player1, new SilundiVision(), "{2}{U}");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(instant.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(instant, land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayDeclineWhenSeveralCardsQualify() {
        IntoTheRoil instant = new IntoTheRoil();
        FieldResearch sorcery = new FieldResearch();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(instant, sorcery, land));
        harness.castFromHand(player1, new SilundiVision(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(instant, sorcery, land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void looksAtOnlySixCardsAndLeavesTheSeventhAboveTheBottomedCards() {
        IntoTheRoil instant = new IntoTheRoil();
        FieldResearch sorcery = new FieldResearch();
        Forest land1 = new Forest();
        Forest land2 = new Forest();
        Forest land3 = new Forest();
        MerfolkWindrobber creature = new MerfolkWindrobber();
        IntoTheRoil seventh = new IntoTheRoil();
        harness.setLibrary(player1, List.of(instant, land1, land2, creature, land3, sorcery, seventh));
        harness.castFromHand(player1, new SilundiVision(), "{2}{U}");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(instant, land1, land2, creature, land3, sorcery);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());
        gd.gameLog.clear();
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrder(instant, land1, land2, creature, land3);
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("Field Research"));
        assertThat(gd.gameLog).noneSatisfy(entry ->
                assertThat(entry.plainText()).contains("Into the Roil"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void nonmatchingCardsGoToTheBottomWithoutAChoice() {
        Forest land = new Forest();
        MerfolkWindrobber creature = new MerfolkWindrobber();
        harness.setLibrary(player1, List.of(land, creature));
        harness.castFromHand(player1, new SilundiVision(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Silundi Vision");
    }

    @Test
    void emptyLibraryDoesNotPreventResolution() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new SilundiVision(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Silundi Vision");
    }
}
