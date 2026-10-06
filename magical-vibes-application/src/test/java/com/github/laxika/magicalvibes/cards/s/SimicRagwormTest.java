package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SimicRagworm.class})
class SimicRagwormTest extends BaseCardTest {

    @Test
    void payingBlueManaUntapsSimicRagworm() {
        Permanent ragworm = addCreatureReady(player1, new SimicRagworm());
        ragworm.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ragworm.isTapped()).isFalse();
    }

    @Test
    void activatingAbilityDoesNotTapSimicRagworm() {
        Permanent ragworm = addCreatureReady(player1, new SimicRagworm());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(ragworm.isTapped()).isFalse();
    }

    @Test
    void activatingAbilityRequiresBlueMana() {
        Permanent ragworm = addCreatureReady(player1, new SimicRagworm());
        ragworm.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ragworm.isTapped()).isTrue();
    }

    @Test
    void canUntapWhileSummoningSick() {
        Permanent ragworm = harness.addToBattlefieldAndReturn(player1, new SimicRagworm());
        ragworm.setSummoningSick(true);
        ragworm.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ragworm.isTapped()).isFalse();
        assertThat(ragworm.isSummoningSick()).isTrue();
    }

    @Test
    void untapsOnlyWhenAbilityResolves() {
        Permanent ragworm = addCreatureReady(player1, new SimicRagworm());
        ragworm.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(ragworm.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(ragworm.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void untapsOnlyTheSourceCreature() {
        Permanent ragworm = addCreatureReady(player1, new SimicRagworm());
        Permanent other = addCreatureReady(player1, new SimicRagworm());
        Permanent opponent = addCreatureReady(player2, new SimicRagworm());
        ragworm.tap();
        other.tap();
        opponent.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ragworm.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(opponent.isTapped()).isTrue();
    }
}
