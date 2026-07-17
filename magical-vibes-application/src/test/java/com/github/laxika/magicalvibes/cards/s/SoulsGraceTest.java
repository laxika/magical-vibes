package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulsGraceTest extends BaseCardTest {

    private void castSoulsGrace(UUID targetId) {
        harness.setHand(player1, List.of(new SoulsGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Controller gains life equal to target creature's power")
    void gainsLifeEqualToPower() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castSoulsGrace(targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Life gain accounts for power modifiers")
    void gainsLifeAccountsForPowerModifiers() {
        harness.setLife(player1, 10);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setPowerModifier(3); // 2 + 3 = 5 effective power

        castSoulsGrace(creature.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // valid target so spell is playable
        UUID landId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();

        harness.setHand(player1, List.of(new SoulsGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
