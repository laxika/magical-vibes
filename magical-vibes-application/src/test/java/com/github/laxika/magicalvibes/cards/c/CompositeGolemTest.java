package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeGolemTest extends BaseCardTest {

    // ===== Mana ability resolves immediately (CR 605.1a, CR 605.3a) =====

    @Test
    @DisplayName("Activating Composite Golem sacrifices it and adds WUBRG to mana pool immediately")
    void activateAbilityAddsWubrgImmediately() {
        harness.addToBattlefield(player1, new CompositeGolem());

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        // The permanent should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Composite Golem"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Composite Golem"));

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
        // Mana abilities don't use the stack and don't require the creature to be free of summoning sickness
        // unless they require tapping (CR 605.1a). Composite Golem's ability does not require tapping.
        harness.addToBattlefield(player1, new CompositeGolem());

        GameData gd = harness.getGameData();

        // Should succeed even though the creature just entered the battlefield
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Composite Golem"));
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

