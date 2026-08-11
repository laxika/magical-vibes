package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RaidersSpoilsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+0")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new RaidersSpoils());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        Permanent opposingBears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Warrior dealing combat damage may be paid for to draw a card")
    void warriorCombatDamageMayPayLifeToDraw() {
        harness.addToBattlefield(player1, new RaidersSpoils());
        Permanent warrior = addReadyWarrior();
        warrior.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombatDamage();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the life payment does not draw a card")
    void decliningLifePaymentDoesNothing() {
        harness.addToBattlefield(player1, new RaidersSpoils());
        Permanent warrior = addReadyWarrior();
        warrior.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombatDamage();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("A non-Warrior dealing combat damage does not trigger the draw ability")
    void nonWarriorDoesNotTrigger() {
        harness.addToBattlefield(player1, new RaidersSpoils());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombatDamage();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private Permanent addReadyWarrior() {
        Permanent warrior = new Permanent(new KraulWarrior());
        warrior.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(warrior);
        return warrior;
    }

    private void resolveCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
