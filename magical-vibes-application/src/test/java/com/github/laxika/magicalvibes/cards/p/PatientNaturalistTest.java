package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ArmoredArmadillo;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatientNaturalist.class, Forest.class, Plains.class, ArmoredArmadillo.class, LeylineOfTheVoid.class})
class PatientNaturalistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards and lets you put one milled land into your hand")
    void returnsOneMilledLandToHand() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, new ArmoredArmadillo(), plains));

        castAndResolve();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).hasSize(2);

        int plainsIndex = gd.playerGraveyards.get(player1.getId()).indexOf(plains);
        harness.handleGraveyardCardChosen(player1, plainsIndex);

        harness.assertInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Armored Armadillo");
    }

    @Test
    @DisplayName("ETB creates a Treasure when no land is milled")
    void createsTreasureWithoutMilledLand() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new ArmoredArmadillo(), new ArmoredArmadillo(), new ArmoredArmadillo()));

        castAndResolve();

        harness.assertOnBattlefield(player1, "Treasure");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    void returnsLandFromShortLibraryWithoutCreatingTreasure() {
        Forest oldForest = new Forest();
        Plains milledPlains = new Plains();
        harness.setGraveyard(player1, List.of(oldForest));
        harness.setLibrary(player1, List.of(milledPlains));

        castAndResolve();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldForest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    @Test
    void emptyLibraryCreatesExactlyOneUntappedTreasure() {
        harness.setLibrary(player1, List.of());

        castAndResolve();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Treasure");
    }

    @Test
    @CardUsed(LeylineOfTheVoid.class)
    void doesNotCreateTreasureWhenMilledLandIsAvailableInFaceUpExile() {
        Forest forest = new Forest();
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        harness.setLibrary(player1, List.of(forest, new ArmoredArmadillo(), new ArmoredArmadillo()));

        castAndResolve();

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new PatientNaturalist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
