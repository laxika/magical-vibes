package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnflinchingCourageTest extends BaseCardTest {

    private Permanent attachCourage(Player controller, Permanent enchanted) {
        Permanent aura = new Permanent(new UnflinchingCourage());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and has trample and lifelink")
    void grantsBoostAndKeywords() {
        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player1.getId()).add(bears);
        attachCourage(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Bonuses fall off when Unflinching Courage leaves the battlefield")
    void bonusesRemovedWhenAuraLeaves() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent aura = attachCourage(player1, bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Unflinching Courage cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new Spellbook()));
        harness.setHand(player1, List.of(new UnflinchingCourage()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID spellbookId = harness.getPermanentId(player2, "Spellbook");
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, spellbookId))
                .isInstanceOf(IllegalStateException.class);
    }
}
