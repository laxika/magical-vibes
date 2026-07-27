package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SilkbindFaerieTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{W/U} and untapping taps target creature")
    void tapsTargetCreatureAndUntapsSource() {
        Permanent faerie = addTapped(player1, new SilkbindFaerie());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        // Paying {Q} untapped the source.
        assertThat(faerie.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new SilkbindFaerie());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addTapped(player1, new SilkbindFaerie());
        Permanent land = addCreatureReady(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.tap();
        return perm;
    }
}
