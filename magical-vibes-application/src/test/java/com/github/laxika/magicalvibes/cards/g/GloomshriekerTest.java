package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gloomshrieker.class, GrizzlyBears.class, Shock.class})
class GloomshriekerTest extends BaseCardTest {

    @Test
    void entersAndReturnsTargetPermanentCardFromGraveyardToHand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castGloomshrieker();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doesNotTargetNonPermanentCardsInGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castGloomshrieker();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void isExiledInsteadOfDying() {
        harness.addToBattlefield(player1, new Gloomshrieker());
        var gloomshrieker = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, gloomshrieker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gloomshrieker");
        harness.assertNotInGraveyard(player1, "Gloomshrieker");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Gloomshrieker"));
    }

    private void castGloomshrieker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Gloomshrieker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
