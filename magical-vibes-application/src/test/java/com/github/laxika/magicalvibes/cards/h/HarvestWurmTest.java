package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GemstoneMine;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarvestWurm.class, GemstoneMine.class, Forest.class, Plains.class})
class HarvestWurmTest extends BaseCardTest {

    private void castHarvestWurm() {
        harness.castFromHand(player1, new HarvestWurm(), "{1}{G}");
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }

    private long basicLandsInHand() {
        return gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Forest") || c.getName().equals("Plains"))
                .count();
    }

    @Test
    @DisplayName("Auto-sacrifices when the graveyard holds no basic land card")
    void autoSacrificesWithoutBasicLandInGraveyard() {
        harness.setGraveyard(player1, List.of(new GemstoneMine()));
        castHarvestWurm();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Harvest Wurm");
        harness.assertInGraveyard(player1, "Harvest Wurm");
    }

    @Test
    @DisplayName("Accepting returns the chosen basic land card and keeps Harvest Wurm")
    void acceptReturnsBasicLandAndKeepsWurm() {
        harness.setGraveyard(player1, List.of(new GemstoneMine(), new Forest(), new Plains()));
        castHarvestWurm();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(choice.validIndices().stream().map(i -> graveyard.get(i).getName()))
                .containsExactlyInAnyOrder("Forest", "Plains");

        int forestIndex = choice.validIndices().stream()
                .filter(i -> graveyard.get(i).getName().equals("Forest"))
                .findFirst().orElseThrow();
        harness.handleGraveyardCardChosen(player1, forestIndex);

        assertThat(basicLandsInHand()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Forest"))).isTrue();
        harness.assertOnBattlefield(player1, "Harvest Wurm");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Does not use a basic land in an opponent's graveyard")
    void doesNotUseOpponentsBasicLand() {
        harness.setGraveyard(player2, List.of(new Forest()));
        castHarvestWurm();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Harvest Wurm");
        harness.assertInGraveyard(player1, "Harvest Wurm");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Declining sacrifices Harvest Wurm and leaves the graveyard untouched")
    void declineSacrificesWurm() {
        harness.setGraveyard(player1, List.of(new Forest()));
        castHarvestWurm();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Harvest Wurm");
        harness.assertInGraveyard(player1, "Harvest Wurm");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(basicLandsInHand()).isZero();
    }
}
