package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({CircleOfTheLandDruid.class, Forest.class, GrizzlyBears.class})
class CircleOfTheLandDruidTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may mill four cards")
    void etbMayMillFourCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new CircleOfTheLandDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Declining the ETB may does not mill")
    void decliningEtbMayDoesNotMill() {
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new CircleOfTheLandDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Death trigger returns a target land card to hand")
    void deathReturnsTargetLandToHand() {
        Card land = new Forest();
        Card nonland = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(land, nonland));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new CircleOfTheLandDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
