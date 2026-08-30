package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatientNaturalist.class, Forest.class, Plains.class, GrizzlyBears.class})
class PatientNaturalistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards and lets you put one milled land into your hand")
    void returnsOneMilledLandToHand() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, new GrizzlyBears(), plains));

        castAndResolve();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).hasSize(2);

        int plainsIndex = gd.playerGraveyards.get(player1.getId()).indexOf(plains);
        harness.handleGraveyardCardChosen(player1, plainsIndex);

        harness.assertInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB creates a Treasure when no land is milled")
    void createsTreasureWithoutMilledLand() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        castAndResolve();

        harness.assertOnBattlefield(player1, "Treasure");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new PatientNaturalist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
