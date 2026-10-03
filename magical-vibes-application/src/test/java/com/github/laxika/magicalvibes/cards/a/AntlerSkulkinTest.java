package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BallynockTrapper;
import com.github.laxika.magicalvibes.cards.c.CinderPyromancer;
import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AntlerSkulkin.class, BallynockTrapper.class, CinderPyromancer.class, ControlMagic.class})
class AntlerSkulkinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability grants persist to a white creature")
    void resolvingGrantsPersistToWhiteCreature() {
        addReadySkulkin(player1);
        Permanent target = addReadyWhiteCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's white creature")
    void canTargetOpponentWhiteCreature() {
        addReadySkulkin(player1);
        Permanent target = addReadyWhiteCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("Persist is removed at end of turn")
    void persistRemovedAtEndOfTurn() {
        addReadySkulkin(player1);
        Permanent target = addReadyWhiteCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.PERSIST)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-white creature")
    void cannotTargetNonWhiteCreature() {
        addReadySkulkin(player1);
        Permanent target = addReadyRedCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a white creature");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadySkulkin(player1);
        Permanent target = addReadyWhiteCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void grantedPersistReturnsCreatureWithoutKeepingTheGrant() {
        addReadySkulkin(player1);
        Permanent target = addReadyWhiteCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, target));
        resolveAllTriggers();

        Permanent returned = findPermanent(player2, "Ballynock Trapper");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(returned.hasKeyword(Keyword.PERSIST)).isFalse();
        harness.assertNotInGraveyard(player2, "Ballynock Trapper");
    }

    @Test
    void grantedPersistDoesNotTriggerWithAMinusCounter() {
        addReadySkulkin(player1);
        Permanent target = addReadyWhiteCreature(player1);
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, target));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ballynock Trapper");
        harness.assertInGraveyard(player1, "Ballynock Trapper");
    }

    @Test
    void repeatedGrantsCreateIndependentPersistTriggers() {
        addReadySkulkin(player1);
        Permanent target = addReadyWhiteCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, target));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Ballynock Trapper")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void abilityCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent skulkin = harness.addToBattlefieldAndReturn(player1, new AntlerSkulkin());
        skulkin.tap();
        Permanent target = addReadyWhiteCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.PERSIST)).isTrue();
        assertThat(skulkin.isTapped()).isTrue();
    }
    @Test
    void persistTriggerIsControlledByTheCreaturesControllerButReturnsToOwner() {
        addReadySkulkin(player1);
        Permanent target = addReadyWhiteCreature(player1);
        harness.addToBattlefieldAndReturn(player2, new ControlMagic()).setAttachedTo(target.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, target));

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getControllerId()).isEqualTo(player2.getId()));
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Ballynock Trapper");
        harness.assertNotOnBattlefield(player2, "Ballynock Trapper");
    }
    private Permanent addReadySkulkin(Player player) {
        return addCreatureReady(player, new AntlerSkulkin());
    }

    private Permanent addReadyWhiteCreature(Player player) {
        return addCreatureReady(player, new BallynockTrapper());
    }

    private Permanent addReadyRedCreature(Player player) {
        return addCreatureReady(player, new CinderPyromancer());
    }

}
