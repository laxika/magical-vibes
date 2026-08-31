package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WirewoodLodge.class, ElvishWarrior.class, GrizzlyBears.class})
class WirewoodLodgeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds one colorless mana")
    void tapForColorlessMana() {
        Permanent lodge = harness.addToBattlefieldAndReturn(player1, new WirewoodLodge());

        harness.activateAbility(player1, 0, null, null);

        assertThat(lodge.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untaps a target Elf controlled by an opponent")
    void untapsTargetElf() {
        Permanent lodge = harness.addToBattlefieldAndReturn(player1, new WirewoodLodge());
        Permanent elf = addCreatureReady(player2, new ElvishWarrior());
        elf.tap();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, elf.getId());
        harness.passBothPriorities();

        assertThat(lodge.isTapped()).isTrue();
        assertThat(elf.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Elf permanent")
    void cannotTargetNonElf() {
        harness.addToBattlefieldAndReturn(player1, new WirewoodLodge());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
    }
}
