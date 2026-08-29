package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RishadanPortTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one colorless mana")
    void manaAbilityAddsColorlessMana() {
        harness.addToBattlefield(player1, new RishadanPort());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanent(player1, "Rishadan Port").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps target land")
    void tapsTargetLand() {
        Permanent port = harness.addToBattlefieldAndReturn(player1, new RishadanPort());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(port.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new RishadanPort());
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
