package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HollowWarrior;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
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

@CardUsed({VerdantField.class, RhysticCave.class, HollowWarrior.class})
class VerdantFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Verdant Field enchants a land")
    void enchantsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        harness.setHand(player1, List.of(new VerdantField()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, List.of(land.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached() && permanent.getAttachedTo().equals(land.getId()));
    }

    @Test
    @DisplayName("Verdant Field cannot enchant a nonland permanent")
    void cannotEnchantNonland() {
        Permanent creature = addCreatureReady(player1, new HollowWarrior());
        harness.setHand(player1, List.of(new VerdantField()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("The enchanted land can tap to give a creature +1/+1")
    void enchantedLandBoostsTargetCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent target = addCreatureReady(player2, new HollowWarrior());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VerdantField());
        aura.setAttachedTo(land.getId());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("The controller of an enchanted land can activate Verdant Field's ability")
    void enchantedLandControllerCanActivateAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        Permanent target = addCreatureReady(player1, new HollowWarrior());
        harness.setHand(player1, List.of(new VerdantField()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off at end of turn and noncreatures cannot be targeted")
    void boostWearsOffAndRejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent target = addCreatureReady(player1, new HollowWarrior());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VerdantField());
        aura.setAttachedTo(land.getId());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);

        land.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, otherLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Verdant Field's granted ability disappears when the Aura leaves")
    void grantedAbilityDisappearsWhenAuraLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent target = addCreatureReady(player2, new HollowWarrior());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VerdantField());
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
    }
}
