package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaidersSpoils.class, GrizzlyBears.class, KraulWarrior.class})
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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombatDamage();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Warrior triggers separately and its payment may be declined independently")
    void multipleWarriorsTriggerIndependently() {
        harness.addToBattlefield(player1, new RaidersSpoils());
        addReadyWarrior().setAttacking(true);
        addReadyWarrior().setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player2, 14);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opposing Warrior dealing combat damage does not trigger the ability")
    void opposingWarriorDoesNotTrigger() {
        harness.addToBattlefield(player1, new RaidersSpoils());
        addCreatureReady(player2, new KraulWarrior()).setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Removing the enchantment ends the boost but does not remove an existing trigger")
    void triggerResolvesAfterEnchantmentLeavesBattlefield() {
        Permanent spoils = harness.addToBattlefieldAndReturn(player1, new RaidersSpoils());
        Permanent warrior = addReadyWarrior();
        warrior.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(spoils);
        gd.playerGraveyards.get(player1.getId()).add(spoils.getCard());
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent addReadyWarrior() {
        return addCreatureReady(player1, new KraulWarrior());
    }

    private void resolveCombatDamage() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
