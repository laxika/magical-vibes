package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CompositeGolem.class, GiantGrowth.class})
class CompositeGolemTest extends BaseCardTest {

    // A mana ability resolves immediately without using the stack (CR 605.1a, CR 605.3b).

    @Test
    @DisplayName("Activating Composite Golem sacrifices it and adds WUBRG to mana pool immediately")
    void activateAbilityAddsWubrgImmediately() {
        harness.addToBattlefield(player1, new CompositeGolem());

        harness.activateAbility(player1, 0, null, null);

        // The permanent should be sacrificed
        harness.assertNotOnBattlefield(player1, "Composite Golem");
        harness.assertInGraveyard(player1, "Composite Golem");

        // Mana ability resolves immediately — no stack entry
        assertThat(gd.stack).isEmpty();

        // All five colors of mana should be in the pool
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Composite Golem can be activated with summoning sickness because its cost has no tap symbol")
    void canActivateWithSummoningSickness() {
        // A creature with summoning sickness may activate an ability without a tap or untap symbol
        // in its cost (CR 302.6). Composite Golem's ability does not require tapping.
        harness.addToBattlefield(player1, new CompositeGolem());

        // Should succeed even though the creature just entered the battlefield
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Composite Golem");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Composite Golem mana can be used to cast spells")
    void manaCanBeUsedToCastSpells() {
        harness.addToBattlefield(player1, new CompositeGolem());
        var target = harness.addToBattlefieldAndReturn(player1, new CompositeGolem());
        harness.setHand(player1, List.of(new GiantGrowth()));

        // Activate to get WUBRG
        harness.activateAbility(player1, 0, null, null);

        // Verify 5 total mana
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);

        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Giant Growth");
        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped Composite Golem can still be sacrificed for mana")
    void canActivateWhileTapped() {
        var golem = harness.addToBattlefieldAndReturn(player1, new CompositeGolem());
        golem.tap();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Composite Golem");
        harness.assertInGraveyard(player1, "Composite Golem");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("Sacrificing Composite Golem resolves immediately while a spell targets it")
    void canActivateWithSpellOnStack() {
        var golem = harness.addToBattlefieldAndReturn(player1, new CompositeGolem());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, golem.getId());
        var spell = gd.stack.getFirst();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Composite Golem");
        harness.assertInGraveyard(player1, "Composite Golem");
        assertThat(gd.stack).containsExactly(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Giant Growth");
    }
}

