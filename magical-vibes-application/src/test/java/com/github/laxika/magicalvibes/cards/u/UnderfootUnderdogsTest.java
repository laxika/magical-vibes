package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AdornedCrocodile;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KrumarInitiate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnderfootUnderdogs.class, KrumarInitiate.class, AdornedCrocodile.class, Forest.class})
class UnderfootUnderdogsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 red Goblin token")
    void enteringBattlefieldCreatesGoblinToken() {
        harness.setHand(player1, List.of(new UnderfootUnderdogs()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.GOBLIN))
                .toList();

        assertThat(tokens).singleElement().satisfies(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("The ability makes a creature you control with power 2 or less unblockable")
    void abilityMakesSmallControlledCreatureUnblockable() {
        Permanent underdogs = addCreatureReady(player1, new UnderfootUnderdogs());
        Permanent target = addCreatureReady(player1, new KrumarInitiate());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(underdogs.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new UnderfootUnderdogs());
        Permanent target = addCreatureReady(player2, new KrumarInitiate());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        addCreatureReady(player1, new UnderfootUnderdogs());
        Permanent target = addCreatureReady(player1, new AdornedCrocodile());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The unblockable effect expires at end of turn")
    void unblockableExpiresAtEndOfTurn() {
        addCreatureReady(player1, new UnderfootUnderdogs());
        Permanent target = addCreatureReady(player1, new KrumarInitiate());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new UnderfootUnderdogs());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItself() {
        Permanent underdogs = addCreatureReady(player1, new UnderfootUnderdogs());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, underdogs.getId());
        harness.passBothPriorities();

        assertThat(underdogs.isTapped()).isTrue();
        assertThat(underdogs.isCantBeBlocked()).isTrue();
    }

    @Test
    void summoningSickSourceCannotActivateTapAbility() {
        Permanent underdogs = harness.addToBattlefieldAndReturn(player1, new UnderfootUnderdogs());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, underdogs.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(underdogs.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent underdogs = addCreatureReady(player1, new UnderfootUnderdogs());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, underdogs.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(underdogs.isTapped()).isFalse();
    }

    @Test
    void powerRestrictionIncludesCountersWhenChoosingTarget() {
        addCreatureReady(player1, new UnderfootUnderdogs());
        Permanent target = addCreatureReady(player1, new KrumarInitiate());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetBecomingTooLargeBeforeResolutionIsIllegal() {
        Permanent underdogs = addCreatureReady(player1, new UnderfootUnderdogs());
        Permanent target = addCreatureReady(player1, new KrumarInitiate());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(underdogs.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetChangingControllerBeforeResolutionIsIllegal() {
        addCreatureReady(player1, new UnderfootUnderdogs());
        Permanent target = addCreatureReady(player1, new KrumarInitiate());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void growingAfterResolutionDoesNotRemoveUnblockability() {
        addCreatureReady(player1, new UnderfootUnderdogs());
        Permanent target = addCreatureReady(player1, new KrumarInitiate());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(target.isCantBeBlocked()).isTrue();
    }
}
