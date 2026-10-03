package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FelidarCub;
import com.github.laxika.magicalvibes.cards.r.RetreatToCoralhelm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoralhelmGuide.class, FelidarCub.class, RetreatToCoralhelm.class})
class CoralhelmGuideTest extends BaseCardTest {

    @Test
    void makesTargetCreatureUnblockable() {
        harness.addToBattlefield(player1, new CoralhelmGuide());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FelidarCub());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void unblockableWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new CoralhelmGuide());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FelidarCub());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void abilityDoesNotTapCoralhelmGuide() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new CoralhelmGuide());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new FelidarCub());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new FelidarCub());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, firstTarget.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(guide.isTapped()).isFalse();
        assertThat(firstTarget.isCantBeBlocked()).isTrue();
        assertThat(secondTarget.isCantBeBlocked()).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new CoralhelmGuide());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new RetreatToCoralhelm());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new CoralhelmGuide());
        guide.setTapped(true);
        guide.setSummoningSick(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, guide.getId());
        harness.passBothPriorities();

        assertThat(guide.isCantBeBlocked()).isTrue();
        assertThat(guide.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new CoralhelmGuide());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, guide.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(guide.isCantBeBlocked()).isFalse();
    }

    @Test
    void cannotActivateWithInsufficientTotalMana() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new CoralhelmGuide());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, guide.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(guide.isCantBeBlocked()).isFalse();
    }

    @Test
    void targetCannotBeBlockedInCombat() {
        Permanent guide = addCreatureReady(player1, new CoralhelmGuide());
        addCreatureReady(player2, new FelidarCub());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, guide.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
