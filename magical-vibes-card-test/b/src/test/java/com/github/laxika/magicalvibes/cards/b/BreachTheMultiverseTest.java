package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PortentTracker;
import com.github.laxika.magicalvibes.cards.w.WrennAndRealmbreaker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreachTheMultiverse.class, GrizzlyBears.class, Island.class,
        PortentTracker.class, WrennAndRealmbreaker.class, DoublingSeason.class})
class BreachTheMultiverseTest extends BaseCardTest {

    @Test
    void millsEachPlayerAndReturnsOneCardFromEachGraveyardUnderItsController() {
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.setLibrary(player1, tenIslands());
        harness.setLibrary(player2, tenIslands());
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new BreachTheMultiverse()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        chooseOnlyCardFromCurrentGraveyard();
        chooseOnlyCardFromCurrentGraveyard();

        Permanent ownPermanent = findPermanentByCardId(ownCreature.getId());
        Permanent opposingPermanent = findPermanentByCardId(opposingCreature.getId());
        assertThat(GameQueryService.permanentHasSubtype(ownPermanent, CardSubtype.PHYREXIAN)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(opposingPermanent, CardSubtype.PHYREXIAN)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(opposingCreature.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotReturnNonCreatureOrPlaneswalkerCards() {
        harness.setLibrary(player1, tenIslands());
        harness.setLibrary(player2, tenIslands());
        harness.setGraveyard(player1, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new BreachTheMultiverse()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getClass().equals(Island.class));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getClass().equals(Island.class));
    }

    @Test
    void canChooseNewlyMilledCardsAndWaitsForBothChoicesBeforeReturningThem() {
        Card ownCreature = new PortentTracker();
        Card opposingCreature = new PortentTracker();
        harness.setLibrary(player1, List.of(ownCreature));
        harness.setLibrary(player2, List.of(opposingCreature));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.castFromHand(player1, new BreachTheMultiverse(), "{5}{B}{B}");
        harness.passBothPriorities();

        chooseOnlyCardFromCurrentGraveyard();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        chooseOnlyCardFromCurrentGraveyard();

        assertThat(findPermanentByCardId(ownCreature.getId()).isTapped()).isFalse();
        assertThat(findPermanentByCardId(opposingCreature.getId()).isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(GameQueryService.permanentHasSubtype(
                findPermanentByCardId(opposingCreature.getId()), CardSubtype.PHYREXIAN)).isTrue();
    }

    @Test
    void returnsAnOpponentsPlaneswalkerWithoutMakingItPhyrexian() {
        Card planeswalker = new WrennAndRealmbreaker();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(planeswalker));
        harness.castFromHand(player1, new BreachTheMultiverse(), "{5}{B}{B}");
        harness.passBothPriorities();

        chooseOnlyCardFromCurrentGraveyard();

        Permanent returned = findPermanentByCardId(planeswalker.getId());
        assertThat(returned.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(GameQueryService.permanentHasSubtype(returned, CardSubtype.PHYREXIAN)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void addsPhyrexianToExistingCreaturesButNotOpposingOrLaterCreatures() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new PortentTracker());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.castFromHand(player1, new BreachTheMultiverse(), "{5}{B}{B}");
        harness.passBothPriorities();

        assertThat(GameQueryService.permanentHasSubtype(existing, CardSubtype.PHYREXIAN)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(existing, CardSubtype.SATYR)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(existing, CardSubtype.SCOUT)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(opposing, CardSubtype.PHYREXIAN)).isFalse();

        harness.castFromHand(player1, new PortentTracker(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent later = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(existing.getId()))
                .findFirst().orElseThrow();
        assertThat(GameQueryService.permanentHasSubtype(later, CardSubtype.PHYREXIAN)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(existing, CardSubtype.PHYREXIAN)).isTrue();
    }

    @Test
    void appliesCounterReplacementEffectsToReturnedPlaneswalkers() {
        Card planeswalker = new WrennAndRealmbreaker();
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(planeswalker));
        harness.castFromHand(player1, new BreachTheMultiverse(), "{5}{B}{B}");
        harness.passBothPriorities();

        chooseOnlyCardFromCurrentGraveyard();

        assertThat(findPermanentByCardId(planeswalker.getId()).getCounterCount(CounterType.LOYALTY))
                .isEqualTo(8);
    }

    @Test
    void millsOnlyTenCardsAndDoesNotChooseCardsStillInLibraries() {
        Card eleventhCard = new PortentTracker();
        List<Card> library = new java.util.ArrayList<>(tenIslands());
        library.add(eleventhCard);
        harness.setLibrary(player1, library);
        harness.setLibrary(player2, tenIslands());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.castFromHand(player1, new BreachTheMultiverse(), "{5}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eleventhCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card instanceof Island)).hasSize(10);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void controllerChoosesExactlyOneEligibleCardFromEachGraveyard() {
        Card unchosen = new PortentTracker();
        Card chosen = new WrennAndRealmbreaker();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(unchosen, new Island(), chosen));
        harness.castFromHand(player1, new BreachTheMultiverse(), "{5}{B}{B}");
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cardPool()).containsExactly(unchosen, chosen);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanentByCardId(chosen.getId()).getCounterCount(CounterType.LOYALTY))
                .isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(unchosen)
                .doesNotContain(chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void chooseOnlyCardFromCurrentGraveyard() {
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cardPool()).hasSize(1);
        harness.handleGraveyardCardChosen(player1, 0);
    }

    private Permanent findPermanentByCardId(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }

    private List<Card> tenIslands() {
        return java.util.stream.IntStream.range(0, 10)
                .mapToObj(index -> (Card) new Island())
                .toList();
    }
}
