package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.s.SkyhunterProwler;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlinkmothInfusion.class, ConjurersBauble.class, SkyhunterProwler.class})
class BlinkmothInfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 12; i++) {
            harness.addToBattlefield(player1, new ConjurersBauble());
        }
        harness.setHand(player1, List.of(new BlinkmothInfusion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 12; i++) {
            harness.addToBattlefield(player2, new ConjurersBauble());
        }
        harness.setHand(player1, List.of(new BlinkmothInfusion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Untaps all artifacts but no non-artifact permanents")
    void untapsAllArtifactsButNotNonArtifacts() {
        List<Permanent> playerArtifacts = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            playerArtifacts.add(harness.addToBattlefieldAndReturn(player1, new ConjurersBauble()));
        }
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new ConjurersBauble());
        Permanent nonArtifact = harness.addToBattlefieldAndReturn(player1, new SkyhunterProwler());

        playerArtifacts.forEach(Permanent::tap);
        opponentArtifact.tap();
        nonArtifact.tap();

        harness.setHand(player1, List.of(new BlinkmothInfusion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(playerArtifacts).allMatch(permanent -> !permanent.isTapped());
        assertThat(opponentArtifact.isTapped()).isFalse();
        assertThat(nonArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Partial affinity leaves the remaining generic and blue mana payable")
    void partialAffinityLeavesRemainingGenericCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new ConjurersBauble());
        }
        harness.setHand(player1, List.of(new BlinkmothInfusion()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Blinkmoth Infusion");
    }

    @Test
    @DisplayName("Excess affinity cannot pay the blue mana requirement")
    void excessAffinityDoesNotReduceColoredCost() {
        for (int i = 0; i < 14; i++) {
            harness.addToBattlefield(player1, new ConjurersBauble());
        }
        harness.setHand(player1, List.of(new BlinkmothInfusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Blinkmoth Infusion");
    }

    @Test
    @DisplayName("Non-artifact permanents do not contribute to affinity")
    void nonArtifactsDoNotReduceCost() {
        for (int i = 0; i < 12; i++) {
            harness.addToBattlefield(player1, new SkyhunterProwler());
        }
        harness.setHand(player1, List.of(new BlinkmothInfusion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Artifacts entering after casting are also untapped on resolution")
    void untapsArtifactsPresentAtResolution() {
        harness.setHand(player1, List.of(new BlinkmothInfusion()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0);

        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ConjurersBauble());
        artifact.tap();
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Blinkmoth Infusion");
    }

    @Test
    @DisplayName("Resolves without any artifacts on the battlefield")
    void resolvesWithoutArtifacts() {
        harness.setHand(player1, List.of(new BlinkmothInfusion()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Blinkmoth Infusion");
    }
}
