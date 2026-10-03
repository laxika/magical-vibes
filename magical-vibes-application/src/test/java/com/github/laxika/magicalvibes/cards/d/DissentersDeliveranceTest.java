package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonedKhopesh;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DissentersDeliverance.class, GrizzlyBears.class, Ornithopter.class, HonedKhopesh.class})
class DissentersDeliveranceTest extends BaseCardTest {

    private void castDeliverance(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DissentersDeliverance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    // ===== Destroy branch =====

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new Ornithopter());
        UUID targetId = harness.getPermanentId(player2, "Ornithopter");
        castDeliverance(targetId);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    // ===== Illegal target =====

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DissentersDeliverance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cycling =====

    @Test
    @DisplayName("Cycling discards Dissenter's Deliverance and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DissentersDeliverance()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dissenter's Deliverance");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy a noncreature artifact controlled by its caster")
    void destroysOwnNoncreatureArtifact() {
        harness.addToBattlefield(player1, new HonedKhopesh());
        castDeliverance(harness.getPermanentId(player1, "Honed Khopesh"));

        harness.assertNotOnBattlefield(player1, "Honed Khopesh");
        harness.assertInGraveyard(player1, "Honed Khopesh");
        harness.assertInGraveyard(player1, "Dissenter's Deliverance");
    }

    @Test
    @DisplayName("Cycling discards as a cost and draws only on resolution")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new DissentersDeliverance()));
        harness.setLibrary(player1, List.of(new DissentersDeliverance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Dissenter's Deliverance");
        harness.assertInGraveyard(player1, "Dissenter's Deliverance");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Dissenter's Deliverance");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the green cycling cost")
    void cyclingRequiresGreenMana() {
        harness.setHand(player1, List.of(new DissentersDeliverance()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Dissenter's Deliverance");
        harness.assertNotInGraveyard(player1, "Dissenter's Deliverance");
        assertThat(gd.stack).isEmpty();
    }
}
