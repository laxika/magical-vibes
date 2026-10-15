package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Fireslinger;
import com.github.laxika.magicalvibes.cards.d.DauthiSlayer;
import com.github.laxika.magicalvibes.cards.s.ShadowRift;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfDiffusion.class, Fireslinger.class, DauthiSlayer.class, ShadowRift.class})
class WallOfDiffusionTest extends BaseCardTest {

    @Test
    @DisplayName("Wall of Diffusion can block a creature with shadow")
    void blocksShadowAttacker() {
        Permanent wall = addCreatureReady(player2, new WallOfDiffusion());
        addCreatureReady(player1, new DauthiSlayer());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Wall of Diffusion still blocks creatures without shadow")
    void blocksNormalAttacker() {
        Permanent wall = addCreatureReady(player2, new WallOfDiffusion());
        addCreatureReady(player1, new Fireslinger());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without the ability still can't block a creature with shadow")
    void plainBlockerCannotBlockShadow() {
        addCreatureReady(player2, new Fireslinger());
        addCreatureReady(player1, new DauthiSlayer());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Defender prevents Wall of Diffusion from attacking")
    void cannotAttack() {
        addCreatureReady(player1, new WallOfDiffusion());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Wall of Diffusion cannot block a shadow attacker")
    void tappedWallCannotBlockShadow() {
        Permanent wall = addCreatureReady(player2, new WallOfDiffusion());
        wall.tap();
        addCreatureReady(player1, new DauthiSlayer());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wall of Diffusion with shadow cannot block a creature without shadow")
    void wallWithShadowCannotBlockNormalAttacker() {
        Permanent wall = addCreatureReady(player2, new WallOfDiffusion());
        addCreatureReady(player1, new Fireslinger());
        giveShadow(wall);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Wall of Diffusion with shadow can still block a shadow creature")
    void wallWithShadowBlocksShadowAttacker() {
        Permanent wall = addCreatureReady(player2, new WallOfDiffusion());
        addCreatureReady(player1, new DauthiSlayer());
        giveShadow(wall);
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    private void giveShadow(Permanent wall) {
        harness.setHand(player1, List.of(new ShadowRift()));
        harness.setLibrary(player1, List.of(new Fireslinger()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, wall.getId());
    }
}
