package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlatoonDispenserTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card at your end step when you control two other creatures")
    void drawsWithTwoOtherCreatures() {
        harness.addToBattlefield(player1, new PlatoonDispenser());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        setDeck(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not draw a card with fewer than two other creatures")
    void doesNotDrawWithOnlyOneOtherCreature() {
        harness.addToBattlefield(player1, new PlatoonDispenser());
        harness.addToBattlefield(player1, new GrizzlyBears());
        setDeck(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Creates a 1/1 colorless Soldier artifact creature token")
    void createsSoldierToken() {
        harness.addToBattlefield(player1, new PlatoonDispenser());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        var soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().isToken()).isTrue();
        assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(gqs.isArtifact(gd, soldier)).isTrue();
        assertThat(gqs.isCreature(gd, soldier)).isTrue();
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, soldier)).isEmpty();
    }

    @Test
    @DisplayName("Unearth returns the dispenser and exiles it at the next end step")
    void unearthReturnsAndExilesAtEndStep() {
        PlatoonDispenser card = new PlatoonDispenser();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        var dispenser = findPermanent(player1, "Platoon Dispenser");
        assertThat(dispenser.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Platoon Dispenser");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Platoon Dispenser"));
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<com.github.laxika.magicalvibes.model.Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
