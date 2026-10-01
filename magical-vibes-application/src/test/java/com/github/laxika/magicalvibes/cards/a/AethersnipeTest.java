package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.Wispmare;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Aethersnipe.class, Island.class, Wispmare.class})
class AethersnipeTest extends BaseCardTest {

    // ===== Hardcast =====

    @Test
    @DisplayName("Hardcast: ETB returns target nonland permanent to owner's hand and Aethersnipe stays")
    void hardcastBouncesTargetAndStays() {
        harness.addToBattlefield(player2, new Wispmare());
        UUID targetId = harness.getPermanentId(player2, "Wispmare");
        harness.setHand(player1, List.of(new Aethersnipe()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Wispmare");
        harness.assertInHand(player2, "Wispmare");
        harness.assertOnBattlefield(player1, "Aethersnipe");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new Wispmare()); // valid target so spell is playable
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new Aethersnipe()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    // ===== Evoke =====

    @Test
    @DisplayName("Evoke: paying only {1}{U}{U}, ETB still bounces the target")
    void evokeBouncesTarget() {
        harness.addToBattlefield(player2, new Wispmare());
        UUID targetId = harness.getPermanentId(player2, "Wispmare");
        harness.setHand(player1, List.of(new Aethersnipe()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertInHand(player2, "Wispmare");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Evoke: Aethersnipe is sacrificed as it enters")
    void evokeSacrificesSelf() {
        harness.addToBattlefield(player2, new Wispmare());
        UUID targetId = harness.getPermanentId(player2, "Wispmare");
        harness.setHand(player1, List.of(new Aethersnipe()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Aethersnipe");
        harness.assertInGraveyard(player1, "Aethersnipe");
    }

    @Test
    @DisplayName("ETB does nothing if its target is gone before resolution")
    void etbDoesNothingIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new Wispmare());
        UUID targetId = harness.getPermanentId(player2, "Wispmare");
        harness.setHand(player1, List.of(new Aethersnipe()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aethersnipe");
    }
}
