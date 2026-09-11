package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvenkingsHarper.class, Forest.class, GrizzlyBears.class})
class ElvenkingsHarperTest extends BaseCardTest {

    @Test
    void abilityMakesTargetCreatureUnblockable() {
        harness.addToBattlefield(player1, new ElvenkingsHarper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void unblockableWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ElvenkingsHarper());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void abilityCannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new ElvenkingsHarper());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
