package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WizenedCenn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvianChangeling.class, WizenedCenn.class})
class AvianChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling gets boost from Wizened Cenn (Kithkin lord) due to being every creature type")
    void changelingGetsSubtypeBoost() {
        harness.addToBattlefield(player1, new WizenedCenn());
        harness.addToBattlefield(player1, new AvianChangeling());

        Permanent changeling = findPermanent(player1, "Avian Changeling");

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(3);
    }

    @Test
    @DisplayName("Avian Changeling cannot be blocked by a ground creature")
    void cannotBeBlockedByGroundCreature() {
        Permanent attacker = addCreatureReady(player1, new AvianChangeling());
        attacker.setAttacking(true);

        addCreatureReady(player2, new WizenedCenn());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block")
                .hasMessageContaining("(flying)");
    }
}
