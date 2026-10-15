package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WebstrikeElite.class, GloriousAnthem.class,
        GrizzlyBears.class, LeoninScimitar.class})
class WebstrikeEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling destroys an artifact with mana value X before the separate draw")
    void cyclingDestroysArtifactWithManaValueXAndDraws() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar()).getId();
        prepareCycling(1);

        harness.activateHandAbility(player1, 0, targetId, 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Webstrike Elite");
        harness.assertNotInHand(player1, "Webstrike Elite");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Webstrike Elite");
    }

    @Test
    @DisplayName("Cycling destroys a target enchantment with mana value X")
    void cyclingDestroysEnchantmentWithManaValueX() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem()).getId();
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
        prepareCycling(1);

        harness.activateHandAbility(player1, 0, null, 1);
        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Webstrike Elite");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Webstrike Elite");
        harness.assertInHand(player1, "Webstrike Elite");
    }

    @Test
    @DisplayName("Cycling cannot target an artifact with mana value less than X")
    void cyclingRejectsArtifactBelowX() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar()).getId();
        prepareCycling(2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, targetId, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling cannot target an enchantment with mana value less than X")
    void cyclingRejectsEnchantmentBelowX() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem()).getId();
        prepareCycling(4);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, targetId, 4))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling may omit the target even when a legal artifact exists")
    void cyclingMayDeclineLegalTarget() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        prepareCycling(1);

        harness.activateHandAbility(player1, 0, null, 1);
        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Webstrike Elite");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInHand(player1, "Webstrike Elite");
    }

    @Test
    @DisplayName("Losing the destruction trigger's target does not prevent the cycling draw")
    void cyclingStillDrawsAfterTargetLeavesBattlefield() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar()).getId();
        prepareCycling(1);
        harness.activateHandAbility(player1, 0, targetId, 1);

        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(targetId));
        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Webstrike Elite");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Webstrike Elite");
    }

    private void prepareCycling(int xValue) {
        harness.setHand(player1, List.of(new WebstrikeElite()));
        harness.setLibrary(player1, List.of(new WebstrikeElite()));
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
