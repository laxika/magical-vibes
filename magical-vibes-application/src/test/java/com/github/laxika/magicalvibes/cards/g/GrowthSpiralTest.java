package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrowthSpiral.class, Forest.class, SauroformHybrid.class, SimicGuildgate.class})
class GrowthSpiralTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and puts a chosen land from hand onto the battlefield untapped")
    void drawsAndPutsLandOntoBattlefield() {
        harness.setHand(player1, List.of(new GrowthSpiral(), new Forest()));
        harness.setLibrary(player1, List.of(new SauroformHybrid()));
        addManaForGrowthSpiral();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isFalse();
        harness.assertInHand(player1, "Sauroform Hybrid");
    }

    @Test
    @DisplayName("Declining the land drop still draws a card")
    void decliningLandDropStillDraws() {
        harness.setHand(player1, List.of(new GrowthSpiral(), new Forest()));
        harness.setLibrary(player1, List.of(new SauroformHybrid()));
        addManaForGrowthSpiral();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Sauroform Hybrid");
    }

    @Test
    void canPutTheLandJustDrawnOntoBattlefield() {
        harness.setHand(player1, List.of(new GrowthSpiral()));
        harness.setLibrary(player1, List.of(new Forest()));
        addManaForGrowthSpiral();

        harness.castAndResolveInstant(player1, 0);
        harness.assertInHand(player1, "Forest");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Growth Spiral");
    }

    @Test
    void puttingLandDoesNotUseOrRequireAnAvailableLandPlay() {
        harness.setHand(player1, List.of(new GrowthSpiral(), new Forest(), new SimicGuildgate()));
        harness.setLibrary(player1, List.of(new SauroformHybrid()));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        addManaForGrowthSpiral();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Simic Guildgate");
        harness.assertInHand(player1, "Sauroform Hybrid");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        harness.assertInGraveyard(player1, "Growth Spiral");
    }

    @Test
    void landWithEntersTappedAbilityStillEntersTapped() {
        harness.setHand(player1, List.of(new GrowthSpiral(), new SimicGuildgate()));
        harness.setLibrary(player1, List.of(new SauroformHybrid()));
        addManaForGrowthSpiral();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Simic Guildgate").isTapped()).isTrue();
        harness.assertInHand(player1, "Sauroform Hybrid");
    }

    @Test
    void acceptingWithNoLandInHandStillFinishesResolution() {
        harness.setHand(player1, List.of(new GrowthSpiral()));
        harness.setLibrary(player1, List.of(new SauroformHybrid()));
        addManaForGrowthSpiral();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sauroform Hybrid");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Growth Spiral");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotChooseANonlandCardToPutOntoBattlefield() {
        harness.setHand(player1, List.of(new GrowthSpiral(), new SauroformHybrid(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        addManaForGrowthSpiral();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Sauroform Hybrid");
        harness.handleCardChosen(player1, 1);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Sauroform Hybrid");
    }

    private void addManaForGrowthSpiral() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
