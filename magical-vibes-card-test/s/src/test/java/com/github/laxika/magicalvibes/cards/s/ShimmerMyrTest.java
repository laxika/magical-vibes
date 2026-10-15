package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.PhyrexianDigester;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShimmerMyr.class, LeoninScimitar.class, GrizzlyBears.class, PhyrexianDigester.class})
class ShimmerMyrTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast artifact spell during opponent's turn with Shimmer Myr on battlefield")
    void canCastArtifactDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new ShimmerMyr());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Player1 can cast artifact with flash timing
        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Leonin Scimitar");
    }

    @Test
    @DisplayName("Can cast artifact spell during combat with Shimmer Myr on battlefield")
    void canCastArtifactDuringCombat() {
        harness.addToBattlefield(player1, new ShimmerMyr());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Leonin Scimitar");
    }

    @Test
    @DisplayName("Cannot cast non-artifact creature at instant speed with Shimmer Myr")
    void cannotCastNonArtifactAtInstantSpeed() {
        harness.addToBattlefield(player1, new ShimmerMyr());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Artifact spells cannot be cast at instant speed without Shimmer Myr")
    void cannotCastArtifactAtInstantSpeedWithoutShimmerMyr() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast artifact creature at instant speed with Shimmer Myr")
    void canCastArtifactCreatureAtInstantSpeed() {
        harness.addToBattlefield(player1, new ShimmerMyr());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        // Shimmer Myr itself is an artifact creature — cast a second one at instant speed
        harness.setHand(player1, List.of(new ShimmerMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shimmer Myr");
    }

    @Test
    @DisplayName("Artifact spells lose flash timing when Shimmer Myr leaves the battlefield")
    void artifactLosesFlashWhenShimmerMyrLeaves() {
        harness.addToBattlefield(player1, new ShimmerMyr());

        // Remove Shimmer Myr from battlefield
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Shimmer Myr only grants flash to its controller's artifact spells")
    void onlyAffectsController() {
        // Player2 controls Shimmer Myr, player1 should not benefit
        harness.addToBattlefield(player2, new ShimmerMyr());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Shimmer Myr can be cast during an opponent's turn without another Myr")
    void ownFlashWorksWithoutBattlefieldGrant() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new ShimmerMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shimmer Myr");
    }

    @Test
    @DisplayName("Shimmer Myr grants flash to an artifact creature without printed flash")
    void grantsFlashToNonFlashArtifactCreature() {
        harness.addToBattlefield(player1, new ShimmerMyr());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new PhyrexianDigester()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Digester");
    }

    @Test
    @DisplayName("Artifacts may be cast in response to another spell and remain legal if Myr leaves")
    void canRespondWithArtifactAndResolveAfterMyrLeaves() {
        harness.addToBattlefield(player1, new ShimmerMyr());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new PhyrexianDigester(), new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.ensurePriority(player1);
        harness.castCreature(player1, 0);
        harness.ensurePriority(player1);

        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(2);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Phyrexian Digester");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shimmer Myr on the stack does not grant flash until it resolves")
    void flashGrantStartsOnlyAfterResolution() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new ShimmerMyr(), new PhyrexianDigester()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.ensurePriority(player1);
        harness.castCreature(player1, 0);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.passBothPriorities();
        harness.ensurePriority(player1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shimmer Myr");
        harness.assertOnBattlefield(player1, "Phyrexian Digester");
    }
}
