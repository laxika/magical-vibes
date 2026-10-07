package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Stonehands.class, BalduvianBears.class, ZuranOrb.class, Disenchant.class})
class StonehandsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +0/+2")
    void enchantedCreatureGetsStaticBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());
        attachStonehands(player1, bearsPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Activating the ability gives enchanted creature +1/+0 until end of turn")
    void activatedAbilityBoostsPower() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());
        attachStonehands(player1, bearsPerm);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ability can be activated multiple times, stacking the power boost")
    void abilityStacksMultipleActivations() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());
        attachStonehands(player1, bearsPerm);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Activated ability boosts an opponent's enchanted creature")
    void activatedAbilityBoostsOpponentsEnchantedCreature() {
        Permanent bearsPerm = addCreatureReady(player2, new BalduvianBears());
        attachStonehands(player1, bearsPerm);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power boost wears off at end of turn but static boost remains")
    void abilityBoostWearsOffAtEndOfTurn() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());
        attachStonehands(player1, bearsPerm);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bearsPerm.getPowerModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creature loses the static boost when Stonehands is removed")
    void staticBoostStopsWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());
        Permanent auraPerm = attachStonehands(player1, bearsPerm);

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new Stonehands()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Stonehands attaches it to the targeted creature")
    void resolvingAuraAttachesToTargetCreature() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new Stonehands()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new BalduvianBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());
        harness.setHand(player1, List.of(new Stonehands()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Only the enchanted creature receives the boosts")
    void boostsDoNotAffectOtherCreatures() {
        Permanent enchanted = addCreatureReady(player1, new BalduvianBears());
        Permanent other = addCreatureReady(player1, new BalduvianBears());
        attachStonehands(player1, enchanted);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("A pending activation still boosts the creature after Stonehands is destroyed")
    void pendingActivationSurvivesAuraDestruction() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        Permanent aura = attachStonehands(player1, bears);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.castAndResolveInstant(player2, 0, aura.getId());

        harness.assertInGraveyard(player1, "Stonehands");
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A resolved power boost remains after Stonehands is destroyed")
    void resolvedBoostSurvivesAuraDestruction() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        Permanent aura = attachStonehands(player1, bears);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, aura.getId());

        harness.assertInGraveyard(player1, "Stonehands");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private Permanent attachStonehands(Player controller, Permanent target) {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(controller, new Stonehands());
        auraPerm.setAttachedTo(target.getId());
        return auraPerm;
    }
}
