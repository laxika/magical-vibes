package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KasminaEnigmaSage;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PestilentCauldron.class, Forest.class, GrizzlyBears.class, KasminaEnigmaSage.class,
        LeoninScimitar.class, WrathOfGod.class})
class PestilentCauldronTest extends BaseCardTest {

    @Test
    void createsPestThatGainsLifeWhenItDies() {
        Permanent cauldron = addReadyCauldron();
        Card discard = new Forest();
        harness.setHand(player1, List.of(discard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent pest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Pest"))
                .findFirst()
                .orElseThrow();
        harness.setHand(player1, List.of(new PestilentCauldron(), new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pest);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(cauldron.isTapped()).isTrue();
    }

    @Test
    void millsOpponentsByLifeGainedThisTurn() {
        addReadyCauldron();
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    void exilesFourCardsFromOneGraveyardAndDraws() {
        Permanent cauldron = addReadyCauldron();
        List<Card> graveyard = List.of(new Forest(), new GrizzlyBears(), new LeoninScimitar(), new Forest());
        harness.setGraveyard(player2, graveyard);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        List<UUID> targets = graveyard.stream().map(Card::getId).toList();
        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, targets);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1).allMatch(card -> card instanceof Forest);
        assertThat(cauldron.isTapped()).isTrue();
    }

    @Test
    void restorativeBurstReturnsUpToTwoAllowedCardsGainsLifeAndExilesItself() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card nonPermanent = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, land, nonPermanent));
        harness.setHand(player1, List.of(new PestilentCauldron()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonPermanent);
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getName().equals("Pestilent Cauldron"));
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getName().equals("Pestilent Cauldron"));
    }

    @Test
    void millsNothingWithoutLifeGain() {
        addReadyCauldron();
        int deckSize = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSize);
    }

    @Test
    void cannotTargetCardsFromDifferentGraveyards() {
        Permanent cauldron = addReadyCauldron();
        List<Card> ownCards = List.of(new Forest(), new Forest());
        List<Card> opponentCards = List.of(new Forest(), new Forest());
        harness.setGraveyard(player1, ownCards);
        harness.setGraveyard(player2, opponentCards);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        List<UUID> targets = List.of(ownCards.get(0).getId(), ownCards.get(1).getId(),
                opponentCards.get(0).getId(), opponentCards.get(1).getId());

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 2, targets))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cauldron.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(ownCards);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(opponentCards);
    }

    @Test
    void cannotActivateExileAbilityWithFewerThanFourTargets() {
        Permanent cauldron = addReadyCauldron();
        List<Card> cards = List.of(new Forest(), new Forest(), new Forest());
        harness.setGraveyard(player1, cards);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 2,
                cards.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cauldron.isTapped()).isFalse();
    }

    @Test
    void drawsAndExilesRemainingCardsWhenOneTargetLeavesGraveyard() {
        addReadyCauldron();
        List<Card> cards = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setGraveyard(player2, cards);
        harness.setHand(player1, List.of());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, cards.stream().map(Card::getId).toList());
        harness.setGraveyard(player2, cards.subList(1, 4));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrderElementsOf(cards.subList(1, 4));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNotDrawWhenAllFourTargetsLeaveGraveyard() {
        addReadyCauldron();
        List<Card> cards = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setGraveyard(player2, cards);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, cards.stream().map(Card::getId).toList());
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void restorativeBurstCanResolveWithoutTargets() {
        Card burst = new PestilentCauldron();
        Card unchosen = new Forest();
        harness.setHand(player1, List.of(burst));
        harness.setGraveyard(player1, List.of(unchosen));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 24);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(burst);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unchosen);
    }

    @Test
    void restorativeBurstCanReturnOnlyOnePlaneswalker() {
        Card planeswalker = new KasminaEnigmaSage();
        harness.setGraveyard(player1, List.of(planeswalker));
        harness.setHand(player1, List.of(new PestilentCauldron()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(planeswalker);
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 24);
    }

    @Test
    void restorativeBurstDoesNotGainLifeOrExileItselfWhenAllTargetsBecomeIllegal() {
        Card target = new Forest();
        Card burst = new PestilentCauldron();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(burst));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(burst);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(burst);
    }

    private Permanent addReadyCauldron() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new PestilentCauldron());
        cauldron.setSummoningSick(false);
        return cauldron;
    }
}
