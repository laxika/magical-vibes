package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SweetOblivion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeistOfRegret.class, CounselOfTheSoratami.class, Forest.class, Shock.class,
        GlimpseOfFreedom.class, GrizzlyBears.class, SweetOblivion.class})
class GeistOfRegretTest extends BaseCardTest {

    @Test
    void putsOneRandomInstantAndSorceryFromLibraryIntoGraveyard() {
        Forest land = new Forest();
        Shock instant = new Shock();
        CounselOfTheSoratami sorcery = new CounselOfTheSoratami();
        GeistOfRegret geist = new GeistOfRegret();
        harness.setLibrary(player1, List.of(land, instant, sorcery));
        harness.setHand(player1, List.of(geist));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(instant, sorcery);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void copiesAnInstantOrSorceryCastFromTheGraveyard() {
        GeistOfRegret geist = new GeistOfRegret();
        GlimpseOfFreedom glimpse = new GlimpseOfFreedom();
        List<GrizzlyBears> exileCost = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        List<Card> drawnCards = List.of(new GrizzlyBears(), new GrizzlyBears());
        harness.addToBattlefield(player1, geist);
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(glimpse, exileCost.get(0), exileCost.get(1),
                exileCost.get(2), exileCost.get(3), exileCost.get(4)));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawnCards);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(glimpse);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(exileCost);
    }

    @Test
    void stillPutsASorceryIntoGraveyardWhenThereIsNoInstant() {
        Forest land = new Forest();
        CounselOfTheSoratami sorcery = new CounselOfTheSoratami();
        harness.setLibrary(player1, List.of(land, sorcery));

        harness.enterBattlefieldAndReturn(player1, new GeistOfRegret());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void putsAnInstantIntoGraveyardWhenThereIsNoSorcery() {
        Forest land = new Forest();
        Shock instant = new Shock();
        harness.setLibrary(player1, List.of(land, instant));

        harness.enterBattlefieldAndReturn(player1, new GeistOfRegret());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void doesNothingToAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new GeistOfRegret());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void choosesOnlyOneCardOfEachTypeAndLeavesOtherCardsInOrder() {
        Shock firstInstant = new Shock();
        Shock secondInstant = new Shock();
        CounselOfTheSoratami firstSorcery = new CounselOfTheSoratami();
        CounselOfTheSoratami secondSorcery = new CounselOfTheSoratami();
        Forest land = new Forest();
        List<Card> library = List.of(firstInstant, firstSorcery, land, secondInstant, secondSorcery);
        harness.setLibrary(player1, library);

        harness.enterBattlefieldAndReturn(player1, new GeistOfRegret());
        resolveAllTriggers();

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).hasSize(2);
        assertThat(graveyard.get(0)).isIn(firstInstant, secondInstant);
        assertThat(graveyard.get(1)).isIn(firstSorcery, secondSorcery);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(
                library.stream().filter(card -> !graveyard.contains(card)).toList());
    }

    @Test
    void doesNotCopyAnInstantCastFromHand() {
        harness.addToBattlefield(player1, new GeistOfRegret());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiesAGraveyardSorceryAndAllowsANewTarget() {
        SweetOblivion sorcery = new SweetOblivion();
        List<Card> exileCost = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        List<Card> ownLibrary = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        List<Card> opponentLibrary = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.addToBattlefield(player1, new GeistOfRegret());
        harness.setGraveyard(player1, List.of(sorcery, exileCost.get(0), exileCost.get(1),
                exileCost.get(2), exileCost.get(3)));
        harness.setLibrary(player1, ownLibrary);
        harness.setLibrary(player2, opponentLibrary);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(), List.of(), List.of(1, 2, 3, 4), null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5)
                .containsAll(ownLibrary).contains(sorcery);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(exileCost);
    }

    @Test
    void canKeepTheOriginalTargetForAGraveyardSorceryCopy() {
        SweetOblivion sorcery = new SweetOblivion();
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.addToBattlefield(player1, new GeistOfRegret());
        harness.setGraveyard(player1, List.of(sorcery, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(), List.of(), List.of(1, 2, 3, 4), null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sorcery);
    }

    @Test
    void doesNotCopyAnOpponentsGraveyardSpell() {
        GlimpseOfFreedom glimpse = new GlimpseOfFreedom();
        GrizzlyBears drawnCard = new GrizzlyBears();
        GrizzlyBears remainingCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new GeistOfRegret());
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(glimpse, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(drawnCard, remainingCard));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player2, 0, List.of(1, 2, 3, 4, 5));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(glimpse);
    }
}
