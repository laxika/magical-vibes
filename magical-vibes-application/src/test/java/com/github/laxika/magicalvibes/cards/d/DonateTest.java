package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DonateTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains control of target permanent you control")
    void targetPlayerGainsControlOfTargetPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId(), targetId));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getId().equals(targetId));
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by another player")
    void cannotTargetPermanentControlledByAnotherPlayer() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId(), targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }
}
