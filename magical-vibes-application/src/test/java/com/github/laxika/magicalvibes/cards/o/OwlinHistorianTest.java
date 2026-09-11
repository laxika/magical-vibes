package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NaturesSpiral;
import com.github.laxika.magicalvibes.cards.n.NayaCharm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OwlinHistorianTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1")
    void entersWithSurveil() {
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.setHand(player1, List.of(new OwlinHistorian()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Gets +1/+1 until end of turn when a card leaves its controller's graveyard")
    void boostsWhenOwnGraveyardCardLeaves() {
        Permanent historian = addCreatureReady(player1, new OwlinHistorian());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new NaturesSpiral()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, card.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(historian.getPowerModifier()).isEqualTo(1);
        assertThat(historian.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when a card leaves an opponent's graveyard")
    void ignoresOpponentGraveyard() {
        Permanent historian = addCreatureReady(player1, new OwlinHistorian());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of(new NayaCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 1, card.getId());
        harness.passBothPriorities();

        assertThat(historian.getPowerModifier()).isZero();
        assertThat(historian.getToughnessModifier()).isZero();
    }
}
