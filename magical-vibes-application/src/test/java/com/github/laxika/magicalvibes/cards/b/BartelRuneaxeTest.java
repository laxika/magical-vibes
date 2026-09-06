package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Rancor;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BartelRuneaxe.class, Rancor.class, GrizzlyBears.class, Shock.class})
class BartelRuneaxeTest extends BaseCardTest {

    @Test
    @DisplayName("Aura spells cannot target Bartel Runeaxe")
    void auraSpellsCannotTargetBartelRuneaxe() {
        Permanent bartel = harness.addToBattlefieldAndReturn(player2, new BartelRuneaxe());

        harness.setHand(player1, List.of(new Rancor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bartel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be enchanted by other Auras");
    }

    @Test
    @DisplayName("Non-Aura spells can target Bartel Runeaxe")
    void nonAuraSpellsCanTargetBartelRuneaxe() {
        Permanent bartel = harness.addToBattlefieldAndReturn(player2, new BartelRuneaxe());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bartel.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Shock"));
    }

    @Test
    @DisplayName("Aura spells can target other creatures")
    void auraSpellsCanTargetOtherCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Rancor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
    }
}
