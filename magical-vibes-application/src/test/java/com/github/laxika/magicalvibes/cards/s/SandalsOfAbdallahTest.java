package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SandalsOfAbdallah.class, GrizzlyBears.class})
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

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sandals of Abdallah");
    }

    @Test
    @DisplayName("Does not destroy itself when the targeted creature survives")
    void doesNotDestroyItselfWhenTargetSurvives() {
        Permanent sandals = addSandals();
        Permanent target = addCreature(player2);

        activate(sandals, target);

        harness.assertOnBattlefield(player1, "Sandals of Abdallah");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
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

    private Permanent addSandals() {
        return harness.addToBattlefieldAndReturn(player1, new SandalsOfAbdallah());
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
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
