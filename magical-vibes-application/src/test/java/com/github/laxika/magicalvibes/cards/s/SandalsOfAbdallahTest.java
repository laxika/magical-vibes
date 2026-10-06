package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BirdMaiden;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandalsOfAbdallah.class, BirdMaiden.class})
class SandalsOfAbdallahTest extends BaseCardTest {

    @Test
    @DisplayName("Grants islandwalk until end of turn")
    void grantsIslandwalkUntilEndOfTurn() {
        Permanent sandals = addSandals();
        Permanent target = addCreature(player2);

        activate(sandals, target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.ISLANDWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Destroys itself when the targeted creature dies this turn")
    void destroysItselfWhenTargetDiesThisTurn() {
        Permanent sandals = addSandals();
        Permanent target = addCreature(player2);

        activate(sandals, target);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, target));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Bird Maiden");
        harness.assertInGraveyard(player1, "Sandals of Abdallah");
    }

    @Test
    @DisplayName("Does not destroy itself when the targeted creature survives")
    void doesNotDestroyItselfWhenTargetSurvives() {
        Permanent sandals = addSandals();
        Permanent target = addCreature(player2);

        activate(sandals, target);

        harness.assertOnBattlefield(player1, "Sandals of Abdallah");
        harness.assertOnBattlefield(player2, "Bird Maiden");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent sandals = addSandals();
        Permanent otherSandals = harness.addToBattlefieldAndReturn(player2, new SandalsOfAbdallah());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(sandals), 0, null, otherSandals.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Death on a later turn does not destroy the artifact")
    void delayedDestructionExpiresAtEndOfTurn() {
        Permanent sandals = addSandals();
        Permanent target = addCreature(player2);
        activate(sandals, target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, target));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Bird Maiden");
        harness.assertOnBattlefield(player1, "Sandals of Abdallah");
    }

    @Test
    @DisplayName("Another creature dying does not trigger destruction")
    void ignoresDeathOfAnotherCreature() {
        Permanent sandals = addSandals();
        Permanent target = addCreature(player2);
        Permanent other = addCreature(player1);
        activate(sandals, target);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, other));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Bird Maiden");
        harness.assertOnBattlefield(player1, "Sandals of Abdallah");
        assertThat(gqs.hasKeyword(gd, target, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Ability resolves even if the artifact leaves the battlefield")
    void grantsIslandwalkAfterSourceLeaves() {
        Permanent sandals = addSandals();
        Permanent target = addCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(sandals), 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, sandals));

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.ISLANDWALK)).isTrue();
        harness.assertInHand(player1, "Sandals of Abdallah");
    }

    @Test
    @DisplayName("Delayed destruction does not destroy a returned artifact")
    void doesNotDestroySourceAfterItLeavesAndReturns() {
        Permanent sandals = addSandals();
        Permanent target = addCreature(player2);
        activate(sandals, target);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, sandals));
        harness.castFromHand(player1, sandals.getCard(), "{4}");
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, target));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Bird Maiden");
        harness.assertOnBattlefield(player1, "Sandals of Abdallah");
    }

    private Permanent addSandals() {
        return harness.addToBattlefieldAndReturn(player1, new SandalsOfAbdallah());
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new BirdMaiden());
    }

    private void activate(Permanent sandals, Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(sandals), 0, null, target.getId());
        harness.passBothPriorities();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
