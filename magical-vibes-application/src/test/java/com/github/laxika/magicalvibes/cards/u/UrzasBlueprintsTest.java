package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrzasBlueprintsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Urza's Blueprints draws a card")
    void tappingDrawsCard() {
        Permanent blueprints = harness.addToBattlefieldAndReturn(player1, new UrzasBlueprints());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(blueprints.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Urza's Blueprints cannot be tapped twice without untapping")
    void cannotActivateWhenTapped() {
        Permanent blueprints = harness.addToBattlefieldAndReturn(player1, new UrzasBlueprints());
        blueprints.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Urza's Blueprints can activate while summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent blueprints = new Permanent(new UrzasBlueprints());
        blueprints.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(blueprints);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(blueprints.isTapped()).isTrue();
    }
}
