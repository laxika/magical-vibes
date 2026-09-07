package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MercilessEnforcers.class)
class MercilessEnforcersTest extends BaseCardTest {

    @Test
    @DisplayName("The ability deals 1 damage to each opponent")
    void abilityDealsDamageToEachOpponent() {
        Permanent enforcers = addReadyEnforcers(player1);
        harness.setLife(player2, 20);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(enforcers.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can be activated while the creature is tapped")
    void abilityDoesNotRequireTapping() {
        Permanent enforcers = addReadyEnforcers(player1);
        enforcers.tap();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The ability requires four mana including a black mana")
    void cannotActivateWithoutAbilityCost() {
        addReadyEnforcers(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addReadyEnforcers(Player player) {
        Permanent perm = new Permanent(new MercilessEnforcers());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
