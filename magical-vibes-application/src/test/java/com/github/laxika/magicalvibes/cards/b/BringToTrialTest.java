package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BringToTrialTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature with power 4 or greater")
    void exilesHighPowerCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        UUID targetId = target.getId();

        harness.setHand(player1, List.of(new BringToTrial()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        harness.setHand(player1, List.of(new BringToTrial()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }
}
