package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AltarOfTheBrood;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebstrikeEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling destroys a target artifact within X and draws a card")
    void cyclingDestroysArtifactWithinXAndDraws() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        prepareCycling(1);

        harness.activateHandAbility(player1, 0, targetId, 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Webstrike Elite");
        harness.assertInHand(player1, "Altar of the Brood");
    }

    @Test
    @DisplayName("Cycling destroys a target enchantment within X")
    void cyclingDestroysEnchantmentWithinX() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        prepareCycling(3);

        harness.activateHandAbility(player1, 0, targetId, 3);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cycling cannot target a permanent above X or a non-artifact non-enchantment")
    void cyclingRejectsIllegalTargets() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareCycling(1);

        UUID expensiveTargetId = harness.getPermanentId(player2, "Glorious Anthem");
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, expensiveTargetId, 1))
                .isInstanceOf(IllegalStateException.class);

        UUID creatureTargetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, creatureTargetId, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling with no legal target still draws a card")
    void cyclingWithNoLegalTargetStillDraws() {
        harness.setHand(player1, List.of(new WebstrikeElite()));
        harness.setLibrary(player1, List.of(new AltarOfTheBrood()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Webstrike Elite");
        harness.assertInHand(player1, "Altar of the Brood");
    }

    private void prepareCycling(int xValue) {
        harness.setHand(player1, List.of(new WebstrikeElite()));
        harness.setLibrary(player1, List.of(new AltarOfTheBrood()));
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
