package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaliceOfLife.class})
class ChaliceOfLifeTest extends BaseCardTest {

    private static final int STARTING_LIFE = GameData.STARTING_LIFE_TOTAL;

    @Nested
    @CardUsed({ChaliceOfLife.class})
    @DisplayName("Front face activation")
    class FrontFaceActivation {

        @Test
        @DisplayName("Gains 1 life when activated")
        void gainsOneLife() {
            addArtifactReady(player1);
            harness.setLife(player1, STARTING_LIFE);

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE + 1);
        }

        @Test
        @DisplayName("Does not transform when life is below 30 after gaining")
        void doesNotTransformBelow30() {
            Permanent chalice = addArtifactReady(player1);
            harness.setLife(player1, 28); // 28 + 1 = 29, below 30

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.getLife(player1.getId())).isEqualTo(29);
            assertThat(chalice.isTransformed()).isFalse();
            assertThat(chalice.getCard().getName()).isEqualTo("Chalice of Life");
        }

        @Test
        @DisplayName("Transforms when life reaches exactly 30 after gaining")
        void transformsAtExactly30() {
            Permanent chalice = addArtifactReady(player1);
            harness.setLife(player1, 29); // 29 + 1 = 30, exactly at threshold

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.getLife(player1.getId())).isEqualTo(30);
            assertThat(chalice.isTransformed()).isTrue();
            assertThat(chalice.getCard().getName()).isEqualTo("Chalice of Death");
        }

        @Test
        @DisplayName("Transforms when life is already above 30 before gaining")
        void transformsAbove30() {
            Permanent chalice = addArtifactReady(player1);
            harness.setLife(player1, 35); // 35 + 1 = 36, well above threshold

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.getLife(player1.getId())).isEqualTo(36);
            assertThat(chalice.isTransformed()).isTrue();
            assertThat(chalice.getCard().getName()).isEqualTo("Chalice of Death");
        }

        @Test
        @DisplayName("Cannot activate when tapped")
        void cannotActivateWhenTapped() {
            Permanent chalice = addArtifactReady(player1);
            chalice.tap();

            org.assertj.core.api.Assertions.assertThatThrownBy(
                    () -> harness.activateAbility(player1, 0, null, null)
            ).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({ChaliceOfLife.class})
    @DisplayName("Back face activation (Chalice of Death)")
    class BackFaceActivation {

        @Test
        @DisplayName("Target opponent loses 5 life")
        void targetOpponentLoses5Life() {
            Permanent chalice = addTransformedChalice(player1);
            harness.setLife(player2, STARTING_LIFE);

            int chaliceIdx = indexOf(player1, chalice);
            harness.activateAbility(player1, chaliceIdx, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.getLife(player2.getId())).isEqualTo(STARTING_LIFE - 5);
        }

        @Test
        @DisplayName("Can target self to lose 5 life")
        void canTargetSelf() {
            Permanent chalice = addTransformedChalice(player1);
            harness.setLife(player1, STARTING_LIFE);

            int chaliceIdx = indexOf(player1, chalice);
            harness.activateAbility(player1, chaliceIdx, null, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE - 5);
        }
    }

    @Test
    void commanderDoesNotTransformBelowFiftyLife() {
        gd.format = DeckFormat.COMMANDER;
        Permanent chalice = addArtifactReady(player1);
        harness.setLife(player1, 40);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 41);
        assertThat(chalice.isTransformed()).isFalse();
    }

    @Test
    void commanderTransformsAtFiftyLife() {
        gd.format = DeckFormat.COMMANDER;
        Permanent chalice = addArtifactReady(player1);
        harness.setLife(player1, 49);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 50);
        assertThat(chalice.isTransformed()).isTrue();
    }

    @Test
    void checksLifeAtResolutionRatherThanActivation() {
        Permanent chalice = addArtifactReady(player1);
        harness.setLife(player1, 29);
        harness.activateAbility(player1, 0, null, null);
        harness.setLife(player1, 28);

        harness.passBothPriorities();

        harness.assertLife(player1, 29);
        assertThat(chalice.isTransformed()).isFalse();
    }

    @Test
    void transformationKeepsArtifactTappedAndBackFaceCanActivateAfterUntapping() {
        Permanent chalice = addArtifactReady(player1);
        harness.setLife(player1, 29);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(chalice.isTransformed()).isTrue();
        assertThat(chalice.isTapped()).isTrue();
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, player2.getId())
        ).isInstanceOf(IllegalStateException.class);

        chalice.untap();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 30);
        harness.assertLife(player2, STARTING_LIFE - 5);
        assertThat(chalice.isTransformed()).isTrue();
    }

    @Test
    void noncreatureArtifactCanActivateWhileSummoningSick() {
        Permanent chalice = harness.addToBattlefieldAndReturn(player1, new ChaliceOfLife());
        chalice.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, STARTING_LIFE + 1);
        assertThat(chalice.isTapped()).isTrue();
    }

    @Test
    void gainsLifeEvenIfSourceLeavesBeforeResolution() {
        Permanent chalice = addArtifactReady(player1);
        harness.setLife(player1, 29);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(chalice);
        gd.playerGraveyards.get(player1.getId()).add(chalice.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 30);
        assertThat(chalice.isTransformed()).isFalse();
        harness.assertNotOnBattlefield(player1, "Chalice of Life");
    }

    @Test
    void reachingThresholdWithoutResolvingAbilityDoesNotTransform() {
        Permanent chalice = addArtifactReady(player1);

        harness.setLife(player1, 30);
        harness.runStateBasedActions();

        assertThat(chalice.isTransformed()).isFalse();
    }

    private Permanent addArtifactReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChaliceOfLife());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addTransformedChalice(Player player) {
        Permanent perm = addArtifactReady(player);
        perm.setCard(perm.getCard().getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
