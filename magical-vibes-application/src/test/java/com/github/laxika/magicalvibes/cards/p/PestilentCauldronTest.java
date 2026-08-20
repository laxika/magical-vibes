package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.castSorcery(player1, 1, 0);
        harness.passBothPriorities();
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

        harness.castModalSorcery(player1, 0, 1, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonPermanent);
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getName().equals("Pestilent Cauldron"));
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getName().equals("Restorative Burst"));
    }

    private Permanent addReadyCauldron() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new PestilentCauldron());
        cauldron.setSummoningSick(false);
        return cauldron;
    }
}
