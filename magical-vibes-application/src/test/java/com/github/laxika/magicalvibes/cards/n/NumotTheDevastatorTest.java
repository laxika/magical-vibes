package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NumotTheDevastator.class, Forest.class, Plains.class})
class NumotTheDevastatorTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{R} destroys up to two targeted lands")
    void payingDestroysTwoTargetedLands() {
        addAttackingNumot();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        resolveCombatToTargetChoice();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.handlePermanentChosen(player1, plains.getId());
        harness.passBothPriorities();
        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(forest.getId())
                        || permanent.getId().equals(plains.getId()));
    }

    @Test
    @DisplayName("Declining the payment leaves the targeted lands on the battlefield")
    void decliningPaymentDoesNothing() {
        addAttackingNumot();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        resolveCombatToTargetChoice();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.handlePermanentChosen(player1, plains.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest, plains);
    }

    @Test
    @DisplayName("A nonland permanent is not a legal target")
    void cannotTargetNonlandPermanent() {
        addAttackingNumot();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent otherNumot = harness.addToBattlefieldAndReturn(player2, new NumotTheDevastator());

        resolveCombatToTargetChoice();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(forest.getId()).doesNotContain(otherNumot.getId());
    }

    @Test
    @DisplayName("The trigger can resolve with no chosen lands")
    void canChooseNoLands() {
        addAttackingNumot();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        resolveCombatToTargetChoice();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
    }

    private void addAttackingNumot() {
        Permanent numot = harness.addToBattlefieldAndReturn(player1, new NumotTheDevastator());
        numot.setSummoningSick(false);
        numot.setAttacking(true);
    }

    private void resolveCombatToTargetChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    private void addPaymentMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
