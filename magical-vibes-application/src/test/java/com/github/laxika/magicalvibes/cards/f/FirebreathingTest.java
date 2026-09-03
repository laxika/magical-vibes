package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({Firebreathing.class, FeralShadow.class, Mountain.class})
class FirebreathingTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Firebreathing attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player1, new FeralShadow());

        harness.setHand(player1, List.of(new Firebreathing()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Firebreathing
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    // ===== Granted ability =====

    @Test
    @DisplayName("Enchanted creature can activate {R}: +1/+0")
    void grantedAbilityBoostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new FeralShadow());
        attachFirebreathing(creature);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activations stack and expire at end of turn")
    void activationsStackAndExpireAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new FeralShadow());
        attachFirebreathing(creature);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can be activated while the enchanted creature is tapped")
    void abilityCanBeActivatedWhileTapped() {
        Permanent creature = addCreatureReady(player1, new FeralShadow());
        attachFirebreathing(creature);
        creature.tap();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the enchanted creature gains the activated ability")
    void onlyEnchantedCreatureGainsAbility() {
        Permanent enchantedCreature = addCreatureReady(player1, new FeralShadow());
        addCreatureReady(player1, new FeralShadow());
        attachFirebreathing(enchantedCreature);

        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability cannot be activated without red mana")
    void cannotActivateWithoutRedMana() {
        Permanent creature = addCreatureReady(player1, new FeralShadow());
        attachFirebreathing(creature);

        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new FeralShadow());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Firebreathing()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void attachFirebreathing(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Firebreathing());
        aura.setAttachedTo(creature.getId());
    }
}
