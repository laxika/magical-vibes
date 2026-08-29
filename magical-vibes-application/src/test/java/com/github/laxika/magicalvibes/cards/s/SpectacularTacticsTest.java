package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectacularTactics.class, AirElemental.class, GrizzlyBears.class})
class SpectacularTacticsTest extends BaseCardTest {

    @Test
    void counterModeBoostsOwnCreatureAndGrantsHexproof() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castMode(0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void counterModeCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castMode(0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void destructionModeDestroysCreatureWithPowerAtLeastFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castMode(1, target.getId());

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void destructionModeRejectsCreatureWithPowerThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castMode(1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    private void castMode(int modeIndex, UUID targetId) {
        harness.setHand(player1, List.of(new SpectacularTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, modeIndex, targetId);
        harness.passBothPriorities();
    }
}
