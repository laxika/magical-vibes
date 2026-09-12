package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BlizzardElemental.class)
class BlizzardElementalTest extends BaseCardTest {

    @Test
    @DisplayName("{3}{U} ability untaps Blizzard Elemental")
    void untapAbilityUntapsSelf() {
        Permanent elemental = addCreatureReady(player1, new BlizzardElemental());
        elemental.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating the ability requires the full {3}{U} cost")
    void requiresFullManaCost() {
        Permanent elemental = addCreatureReady(player1, new BlizzardElemental());
        elemental.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(elemental.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps only the Blizzard Elemental whose ability resolves")
    void untapAbilityOnlyUntapsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BlizzardElemental());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BlizzardElemental());
        source.tap();
        other.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }
}
