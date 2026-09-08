package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CopyEnchantment.class, GloriousAnthem.class, GrizzlyBears.class})
class CopyEnchantmentTest extends BaseCardTest {

    @Test
    @DisplayName("May enter as a copy of an enchantment on the battlefield")
    void copiesAnEnchantment() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        CopyEnchantment copy = new CopyEnchantment();
        castCopyEnchantment(copy);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, anthem.getId());

        Permanent entered = findCopy(copy);
        assertThat(entered.getCard().getName()).isEqualTo("Glorious Anthem");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining to copy leaves the enchantment unchanged")
    void declinesToCopy() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        CopyEnchantment copy = new CopyEnchantment();
        castCopyEnchantment(copy);

        harness.handleMayAbilityChosen(player1, false);

        Permanent entered = findCopy(copy);
        assertThat(entered.getCard().getName()).isEqualTo("Copy Enchantment");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private void castCopyEnchantment(CopyEnchantment copy) {
        harness.setHand(player1, List.of(copy));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findCopy(CopyEnchantment copy) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(copy.getId()))
                .findFirst()
                .orElseThrow();
    }
}
