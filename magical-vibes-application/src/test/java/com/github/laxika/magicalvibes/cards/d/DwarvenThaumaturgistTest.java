package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DwarvenThaumaturgistTest extends BaseCardTest {

    private void addThaumaturgistReady() {
        harness.addToBattlefield(player1, new DwarvenThaumaturgist());
        findPermanent(player1, "Dwarven Thaumaturgist").setSummoningSick(false);
    }

    @Test
    @DisplayName("Switches target creature's power and toughness")
    void switchesTargetPowerAndToughness() {
        addThaumaturgistReady();
        harness.addToBattlefield(player2, new GiantSpider());

        UUID targetId = harness.getPermanentId(player2, "Giant Spider");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent spider = findPermanent(player2, "Giant Spider");
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Switch wears off at end of turn")
    void switchWearsOff() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addThaumaturgistReady();
        harness.addToBattlefield(player1, new GiantSpider());

        UUID targetId = harness.getPermanentId(player1, "Giant Spider");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent spider = findPermanent(player1, "Giant Spider");
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addThaumaturgistReady();
        harness.addToBattlefield(player2, new Forest());

        UUID targetId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
    }
}
