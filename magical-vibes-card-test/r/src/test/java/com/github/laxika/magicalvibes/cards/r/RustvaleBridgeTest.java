package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RustvaleBridge.class, StoneRain.class})
class RustvaleBridgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new RustvaleBridge()));

        harness.playLand(player1, 0);

        Permanent bridge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bridge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds red or white mana")
    void manaAbilityAddsRedOrWhiteMana() {
        Permanent bridge = addReadyBridge();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(bridge.isTapped()).isTrue();

        bridge.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Indestructible keeps it on the battlefield through a destroy effect")
    void survivesDestruction() {
        harness.addToBattlefield(player2, new RustvaleBridge());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Rustvale Bridge");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Rustvale Bridge");
    }

    @Test
    @DisplayName("Cannot activate the mana ability while tapped after entering")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new RustvaleBridge()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An untapped bridge can produce mana the turn it enters without using the stack")
    void producesManaImmediatelyAfterUntappingOnEntryTurn() {
        harness.setHand(player1, List.of(new RustvaleBridge()));
        harness.playLand(player1, 0);
        Permanent bridge = gd.playerBattlefields.get(player1.getId()).getFirst();
        bridge.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(bridge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyBridge() {
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new RustvaleBridge());
        bridge.setSummoningSick(false);
        return bridge;
    }
}
