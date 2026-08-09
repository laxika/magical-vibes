package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DominateTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of a creature with mana value X or less")
    void gainsControlOfCreatureWithinManaValueLimit() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castDominate(2, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(bearsId));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bearsId));
        assertThat(gd.newestControlEffectFor(bearsId).duration()).isEqualTo(EffectDuration.PERMANENT);
    }

    @Test
    @DisplayName("Can gain control of a creature with lower mana value than X")
    void gainsControlOfLowerManaValueCreature() {
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        castDominate(2, elvesId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(elvesId));
    }

    @Test
    @DisplayName("Rejects a creature with mana value greater than X")
    void rejectsCreatureAboveManaValueLimit() {
        harness.addToBattlefield(player2, new SerraAngel());
        UUID angelId = harness.getPermanentId(player2, "Serra Angel");

        assertThatThrownBy(() -> castDominate(2, angelId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDominate(int xValue, UUID targetId) {
        harness.setHand(player1, List.of(new Dominate()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 3);
        harness.castInstant(player1, 0, xValue, targetId);
    }
}
