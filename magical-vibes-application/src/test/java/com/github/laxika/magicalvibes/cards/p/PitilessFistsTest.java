package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PitilessFistsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoosted() {
        Permanent giant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(giant);
        Permanent aura = new Permanent(new PitilessFists());
        aura.setAttachedTo(giant.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(5);
    }

    @Test
    @DisplayName("The ETB ability makes the enchanted creature fight an opposing creature")
    void etbAbilityFights() {
        Permanent giant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(giant);
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new PitilessFists()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ETB ability resolves without an opposing creature")
    void etbAbilityResolvesWithoutOpposingCreature() {
        Permanent giant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(giant);

        harness.setHand(player1, List.of(new PitilessFists()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The Aura can enchant only a creature its controller controls")
    void cannotEnchantOpponentCreature() {
        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        harness.setHand(player1, List.of(new PitilessFists()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
