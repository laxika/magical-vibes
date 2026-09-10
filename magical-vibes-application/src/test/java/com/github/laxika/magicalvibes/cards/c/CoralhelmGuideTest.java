package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoralhelmGuide.class, GrizzlyBears.class, Mountain.class})
class CoralhelmGuideTest extends BaseCardTest {

    @Test
    void makesTargetCreatureUnblockable() {
        harness.addToBattlefield(player1, new CoralhelmGuide());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void unblockableWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new CoralhelmGuide());
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
    void abilityDoesNotTapCoralhelmGuide() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new CoralhelmGuide());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, firstTarget.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(guide.isTapped()).isFalse();
        assertThat(firstTarget.isCantBeBlocked()).isTrue();
        assertThat(secondTarget.isCantBeBlocked()).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new CoralhelmGuide());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
