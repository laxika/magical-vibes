package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BallynockCohort;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SinkingFeeling.class, BallynockCohort.class, Island.class})
class SinkingFeelingTest extends BaseCardTest {

    // ===== Targeting =====

    @Test
    @DisplayName("Can enchant any creature")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player2, new BallynockCohort());

        harness.setHand(player1, List.of(new SinkingFeeling()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.setHand(player1, List.of(new SinkingFeeling()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolving Sinking Feeling attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent bears = addCreatureReady(player2, new BallynockCohort());

        harness.setHand(player1, List.of(new SinkingFeeling()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sinking Feeling").getAttachedTo())
                .isEqualTo(bears.getId());
    }

    // ===== Doesn't untap during controller's untap step =====

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent bears = addCreatureReady(player2, new BallynockCohort());
        bears.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinkingFeeling());
        aura.setAttachedTo(bears.getId());

        advanceToUpkeep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature untaps again after Sinking Feeling is removed")
    void creatureUntapsAfterRemoval() {
        Permanent bears = addCreatureReady(player2, new BallynockCohort());
        bears.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinkingFeeling());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToUpkeep(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    // ===== Granted ability: untap by putting a -1/-1 counter =====

    @Test
    @DisplayName("Enchanted creature can pay {1} and a -1/-1 counter to untap itself")
    void grantedAbilityUntapsWithMinusCounter() {
        Permanent bears = addCreatureReady(player2, new BallynockCohort());
        bears.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinkingFeeling());
        aura.setAttachedTo(bears.getId());

        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, null);

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        // Untapped by the ability
        assertThat(bears.isTapped()).isFalse();
        // A -1/-1 counter shrinks the 2/2 Ballynock Cohort to 1/1
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Granted ability can be activated while the creature is untapped and summoning sick")
    void canActivateWhileUntappedAndSummoningSick() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BallynockCohort());
        creature.setSummoningSick(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinkingFeeling());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, null);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated untap ability still resolves after Sinking Feeling leaves")
    void activatedAbilitySurvivesAuraRemoval() {
        Permanent creature = addCreatureReady(player2, new BallynockCohort());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinkingFeeling());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, null);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Granted ability cannot be activated without paying mana")
    void cannotActivateWithoutMana() {
        Permanent creature = addCreatureReady(player2, new BallynockCohort());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinkingFeeling());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Ability is lost when the aura leaves =====

    @Test
    @DisplayName("Creature loses the granted untap ability when Sinking Feeling is removed")
    void abilityLostWhenRemoved() {
        Permanent bears = addCreatureReady(player2, new BallynockCohort());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinkingFeeling());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
