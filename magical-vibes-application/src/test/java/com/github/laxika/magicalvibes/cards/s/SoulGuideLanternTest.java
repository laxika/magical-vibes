package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulGuideLantern.class, GrizzlyBears.class, Shock.class})
class SoulGuideLanternTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability exiles a card from a graveyard")
    void etbExilesTargetCard() {
        Card target = new GrizzlyBears();
        Card remaining = new Shock();
        harness.setGraveyard(player2, List.of(target, remaining));
        harness.setHand(player1, List.of(new SoulGuideLantern()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Its first activated ability exiles each opponent's graveyard and sacrifices it")
    void exilesEachOpponentsGraveyard() {
        Card opponentCard = new GrizzlyBears();
        Card ownCard = new Shock();
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new SoulGuideLantern());
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lantern), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lantern);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lantern.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Its second activated ability sacrifices it and draws a card")
    void sacrificesItselfAndDraws() {
        Card draw = new Shock();
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new SoulGuideLantern());
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lantern), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lantern);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lantern.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }
}
