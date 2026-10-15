package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UmbralMantle.class, SafeholdElite.class})
class UmbralMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Umbral Mantle to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent mantle = addMantleReady(player1);
        Permanent creature = addCreatureReady(player1, new SafeholdElite());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mantle.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Tapped equipped creature can untap and pay {3} to get +2/+2")
    void grantedAbilityGivesBoost() {
        Permanent creature = addCreatureReady(player1, new SafeholdElite());
        creature.tap();
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        // {Q} untaps the creature as part of the cost
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new SafeholdElite());
        creature.tap();
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        advanceToNextTurn(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Untapped equipped creature cannot activate the granted ability")
    void cannotActivateWhenUntapped() {
        Permanent creature = addCreatureReady(player1, new SafeholdElite());
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @Test
    @DisplayName("Creature loses granted ability when Umbral Mantle is removed")
    void creatureLosesAbilityWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new SafeholdElite());
        creature.tap();
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gd.playerBattlefields.get(player1.getId()).remove(mantle);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent mantle = addMantleReady(player1);
        Permanent creature = addCreatureReady(player2, new SafeholdElite());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mantle.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipAtInstantSpeed() {
        addMantleReady(player1);
        Permanent creature = addCreatureReady(player1, new SafeholdElite());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickCreatureCannotPayUntapCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        creature.setSummoningSick(true);
        creature.tap();
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientManaDoesNotUntapCreature() {
        Permanent creature = addCreatureReady(player1, new SafeholdElite());
        creature.tap();
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void untapIsPaidImmediatelyAndPendingBoostSurvivesEquipmentRemoval() {
        Permanent creature = addCreatureReady(player1, new SafeholdElite());
        creature.tap();
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        gd.playerBattlefields.get(player1.getId()).remove(mantle);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void reequippingTransfersAbilityButNotResolvedBoost() {
        Permanent original = addCreatureReady(player1, new SafeholdElite());
        Permanent other = addCreatureReady(player1, new SafeholdElite());
        original.tap();
        other.tap();
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, null, other.getId());
        harness.passBothPriorities();

        assertThat(mantle.getAttachedTo()).isEqualTo(other.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        original.tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(other.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @Test
    void repeatedActivationsStackBoosts() {
        Permanent creature = addCreatureReady(player1, new SafeholdElite());
        creature.tap();
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        creature.tap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void grantedAbilityCanBeActivatedDuringOpponentsTurn() {
        Permanent creature = addCreatureReady(player1, new SafeholdElite());
        creature.tap();
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    private Permanent addMantleReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new UmbralMantle());
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
    }
}
