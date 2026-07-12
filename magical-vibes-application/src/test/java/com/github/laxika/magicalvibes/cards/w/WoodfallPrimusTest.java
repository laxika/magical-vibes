package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WoodfallPrimusTest extends BaseCardTest {

    private void castWoodfallPrimus(UUID targetId) {
        harness.setHand(player1, List.of(new WoodfallPrimus()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);
    }

    // ===== ETB destroys noncreature permanents =====

    @Test
    @DisplayName("ETB destroys target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        castWoodfallPrimus(harness.getPermanentId(player2, "Leonin Scimitar"));

        // Resolve creature spell, then ETB triggered ability
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Woodfall Primus");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("ETB destroys target enchantment")
    void etbDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        castWoodfallPrimus(harness.getPermanentId(player2, "Glorious Anthem"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("ETB destroys target land")
    void etbDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        castWoodfallPrimus(harness.getPermanentId(player2, "Forest"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    // ===== Target restriction =====

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WoodfallPrimus()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature permanent");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        castWoodfallPrimus(harness.getPermanentId(player2, "Leonin Scimitar"));

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can cast without a target when only creatures exist")
    void canCastWithoutTargetWhenOnlyCreaturesExist() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WoodfallPrimus()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Woodfall Primus");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new WoodfallPrimus()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Woodfall Primus");
        assertThat(gd.stack).isEmpty();
    }
}
