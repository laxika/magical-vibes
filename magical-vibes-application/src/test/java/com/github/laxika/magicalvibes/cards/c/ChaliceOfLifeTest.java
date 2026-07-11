package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChaliceOfLifeTest extends BaseCardTest {

    private static final int STARTING_LIFE = GameData.STARTING_LIFE_TOTAL;

    // ===== Front face: {T}: You gain 1 life =====

    @Nested
    @DisplayName("Front face activation")
    class FrontFaceActivation {

        @Test
        @DisplayName("Gains 1 life when activated")
        void gainsOneLife() {
            Permanent chalice = addArtifactReady(player1);
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
        @DisplayName("Cannot activate when tapped (summoning sick artifact)")
        void cannotActivateWhenTapped() {
            ChaliceOfLife card = new ChaliceOfLife();
            Permanent chalice = new Permanent(card);
            // Summoning sick by default; tap-based abilities on artifacts
            // don't care about summoning sickness (only creatures do),
            // but we must manually tap to test
            chalice.setSummoningSick(false);
            chalice.tap();
            gd.playerBattlefields.get(player1.getId()).add(chalice);

            org.assertj.core.api.Assertions.assertThatThrownBy(
                    () -> harness.activateAbility(player1, 0, null, null)
            ).isInstanceOf(IllegalStateException.class);
        }
    }

    // ===== Back face: {T}: Target player loses 5 life =====

    @Nested
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

    // ===== Helpers =====

    private Permanent addArtifactReady(Player player) {
        ChaliceOfLife card = new ChaliceOfLife();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTransformedChalice(Player player) {
        ChaliceOfLife card = new ChaliceOfLife();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
