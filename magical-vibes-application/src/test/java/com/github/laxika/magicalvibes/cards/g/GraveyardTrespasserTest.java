package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveyardTrespasser.class, GraveyardGlutton.class, GrizzlyBears.class, Shock.class})
class GraveyardTrespasserTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles a creature card and applies the life changes")
    void etbExilesCreatureCard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        castTrespasser();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("ETB does not apply the life changes for a noncreature card")
    void etbExilesNoncreatureCardWithoutLifeChanges() {
        Card shock = new Shock();
        harness.setGraveyard(player2, new ArrayList<>(List.of(shock)));
        castTrespasser();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(shock);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The back face applies its life rider once per creature card")
    void backFaceScalesLifeChangesPerCreatureCard() {
        gd.dayNight = DayNight.NIGHT;
        Card ownBears = new GrizzlyBears();
        Card opponentBears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownBears)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentBears)));
        castTrespasser();
        harness.passBothPriorities();

        Permanent glutton = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(glutton.isTransformed()).isTrue();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ownBears.getId(), opponentBears.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownBears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentBears);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Day and night transform the card's faces")
    void dayAndNightTransformTheFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent trespasser = harness.addToBattlefieldAndReturn(player1, new GraveyardTrespasser());

        gd.spellsCastLastTurn.clear();
        advanceToUntap(player1);
        assertThat(trespasser.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUntap(player2);
        assertThat(trespasser.isTransformed()).isFalse();
    }

    private void castTrespasser() {
        harness.setHand(player1, List.of(new GraveyardTrespasser()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void advanceToUntap(Player activePlayer) {
        harness.performUntapStep(activePlayer);
    }
}
