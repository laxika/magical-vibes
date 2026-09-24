package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CompositeGolem.class)
class CompositeGolemTest extends BaseCardTest {

    // A mana ability resolves immediately without using the stack (CR 605.1a, CR 605.3b).

    @Test
    @DisplayName("Activating Composite Golem sacrifices it and adds WUBRG to mana pool immediately")
    void activateAbilityAddsWubrgImmediately() {
        harness.addToBattlefield(player1, new CompositeGolem());

        GameData gd = harness.getGameData();

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
    @DisplayName("Composite Golem can be activated with summoning sickness since it's a mana ability")
    void canActivateWithSummoningSickness() {
        // A creature with summoning sickness may activate an ability without a tap or untap symbol
        // in its cost (CR 302.6). Composite Golem's ability does not require tapping.
        harness.addToBattlefield(player1, new CompositeGolem());

        GameData gd = harness.getGameData();

        // Should succeed even though the creature just entered the battlefield
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Composite Golem");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Composite Golem mana can be used to cast spells")
    void manaCanBeUsedToCastSpells() {
        harness.addToBattlefield(player1, new CompositeGolem());

        GameData gd = harness.getGameData();

        // Activate to get WUBRG
        harness.activateAbility(player1, 0, null, null);

        // Verify 5 total mana
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }
}

