package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltingSnakeskin.class, GrizzlyBears.class, Shock.class, Swamp.class})
class MoltingSnakeskinTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+0 and the regeneration ability")
    void boostsAndGrantsRegeneration() {
        Permanent bears = addEnchantedBears();
        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        harness.activateAbility(player1, bearsIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration saves the enchanted creature from lethal damage")
    void regenerationSavesEnchantedCreature() {
        Permanent bears = addEnchantedBears();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        harness.activateAbility(player1, bearsIndex, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(bears.getRegenerationShield()).isZero();
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The boost and granted ability disappear when the Aura leaves")
    void effectsDisappearWhenAuraLeaves() {
        Permanent bears = addEnchantedBears();
        Permanent aura = findPermanent(player1, "Molting Snakeskin");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gs.getEffectiveActivatedAbilities(gd, bears)).isNotEmpty();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gs.getEffectiveActivatedAbilities(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("Molting Snakeskin cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new MoltingSnakeskin()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent swamp = findPermanent(player1, "Swamp");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An opposing creature's controller can activate the granted ability")
    void opponentControlsGrantedAbility() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltingSnakeskin()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Molting Snakeskin").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        int bearsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);

        harness.activateAbility(player2, bearsIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("An activated regeneration ability still resolves after the Aura leaves")
    void regenerationResolvesAfterAuraLeaves() {
        Permanent bears = addEnchantedBears();
        Permanent aura = findPermanent(player1, "Molting Snakeskin");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        harness.activateAbility(player1, bearsIndex, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gs.getEffectiveActivatedAbilities(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("The regeneration ability requires black mana")
    void regenerationRequiresBlackMana() {
        Permanent bears = addEnchantedBears();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        assertThatThrownBy(() -> harness.activateAbility(player1, bearsIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bears.getRegenerationShield()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addEnchantedBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MoltingSnakeskin());
        aura.setAttachedTo(bears.getId());
        return bears;
    }
}
