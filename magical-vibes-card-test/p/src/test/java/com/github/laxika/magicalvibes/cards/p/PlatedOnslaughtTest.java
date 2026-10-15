package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AdaptiveSporesinger;
import com.github.laxika.magicalvibes.cards.s.SurgicalSkullbomb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlatedOnslaught.class, AdaptiveSporesinger.class, SurgicalSkullbomb.class})
class PlatedOnslaughtTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new SurgicalSkullbomb());
        }
        harness.setHand(player1, List.of(new PlatedOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent artifacts do not reduce the cost")
    void opponentArtifactsDoNotReduceCost() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new SurgicalSkullbomb());
        }
        harness.setHand(player1, List.of(new PlatedOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Boosts your creatures but not your opponent's creatures")
    void boostsOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new AdaptiveSporesinger());
        harness.addToBattlefield(player2, new AdaptiveSporesinger());
        addArtifacts(player1, 3);
        harness.setHand(player1, List.of(new PlatedOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanent(player1, "Adaptive Sporesinger").getEffectivePower()).isEqualTo(4);
        assertThat(findPermanent(player1, "Adaptive Sporesinger").getEffectiveToughness()).isEqualTo(3);
        assertThat(findPermanent(player2, "Adaptive Sporesinger").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player2, "Adaptive Sporesinger").getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AdaptiveSporesinger());
        addArtifacts(player1, 3);
        harness.setHand(player1, List.of(new PlatedOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Adaptive Sporesinger").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Adaptive Sporesinger").getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void partialAffinityStillRequiresRemainingGenericMana() {
        addArtifacts(player1, 2);
        harness.setHand(player1, List.of(new PlatedOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void excessAffinityDoesNotReduceColoredCost() {
        addArtifacts(player1, 5);
        harness.setHand(player1, List.of(new PlatedOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void creaturesAreChosenAtResolutionAndLaterCreaturesAreNotBoosted() {
        addArtifacts(player1, 3);
        harness.setHand(player1, List.of(new PlatedOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0);

        var presentAtResolution = harness.addToBattlefieldAndReturn(player1, new AdaptiveSporesinger());
        harness.passBothPriorities();
        var arrivingLater = harness.addToBattlefieldAndReturn(player1, new AdaptiveSporesinger());

        assertThat(presentAtResolution.getEffectivePower()).isEqualTo(4);
        assertThat(presentAtResolution.getEffectiveToughness()).isEqualTo(3);
        assertThat(arrivingLater.getEffectivePower()).isEqualTo(2);
        assertThat(arrivingLater.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void resolvesWithoutCreaturesAndDoesNotBoostLaterCreatures() {
        addArtifacts(player1, 3);
        harness.setHand(player1, List.of(new PlatedOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);
        var arrivingLater = harness.addToBattlefieldAndReturn(player1, new AdaptiveSporesinger());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Plated Onslaught");
        assertThat(arrivingLater.getEffectivePower()).isEqualTo(2);
        assertThat(arrivingLater.getEffectiveToughness()).isEqualTo(2);
    }

    private void addArtifacts(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new SurgicalSkullbomb());
        }
    }

}
