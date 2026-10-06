package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelflessCathar.class, WalkingCorpse.class})
class SelflessCatharTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new SelflessCathar());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability requires {1}{W} mana")
    void requiresMana() {
        addCreatureReady(player1, new SelflessCathar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability resolves after sacrificing Selfless Cathar as a cost and boosts own creatures +1/+1")
    void resolvingSacrificesAndBoosts() {
        addCreatureReady(player1, new SelflessCathar());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Selfless Cathar is sacrificed
        harness.assertNotOnBattlefield(player1, "Selfless Cathar");
        harness.assertInGraveyard(player1, "Selfless Cathar");

        // Walking Corpse (2/2) gets +1/+1 = 3/3
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        addCreatureReady(player1, new SelflessCathar());
        Permanent ownBears = addCreatureReady(player1, new WalkingCorpse());
        Permanent oppBears = addCreatureReady(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Own creature is boosted
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);

        // Opponent's creature is NOT boosted
        assertThat(gqs.getEffectivePower(gd, oppBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oppBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        addCreatureReady(player1, new SelflessCathar());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, while the boost waits for resolution")
    void sacrificeIsAnActivationCost() {
        addCreatureReady(player1, new SelflessCathar());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Selfless Cathar");
        harness.assertInGraveyard(player1, "Selfless Cathar");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tapped, summoning-sick Cathar can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent cathar = harness.addToBattlefieldAndReturn(player1, new SelflessCathar());
        cathar.setSummoningSick(true);
        cathar.tap();
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Selfless Cathar");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost affects creatures present at resolution, not those entering afterward")
    void affectedCreaturesAreDeterminedAtResolution() {
        addCreatureReady(player1, new SelflessCathar());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(2);
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability resolves with no creatures remaining")
    void resolvesWithoutRemainingCreatures() {
        addCreatureReady(player1, new SelflessCathar());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Selfless Cathar");
    }

    @Test
    @DisplayName("Two generic mana cannot replace the required white mana")
    void requiresWhiteMana() {
        addCreatureReady(player1, new SelflessCathar());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Selfless Cathar");
        harness.assertNotInGraveyard(player1, "Selfless Cathar");
        assertThat(gd.stack).isEmpty();
    }
}
