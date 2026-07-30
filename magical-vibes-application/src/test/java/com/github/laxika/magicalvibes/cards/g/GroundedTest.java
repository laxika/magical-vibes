package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroundedTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature loses flying")
    void enchantedCreatureLosesFlying() {
        Permanent hawk = new Permanent(new SuntailHawk());
        gd.playerBattlefields.get(player2.getId()).add(hawk);
        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isTrue();

        Permanent aura = new Permanent(new Grounded());
        aura.setAttachedTo(hawk.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Other creatures keep flying")
    void otherCreaturesKeepFlying() {
        Permanent enchanted = new Permanent(new SuntailHawk());
        Permanent other = new Permanent(new SuntailHawk());
        gd.playerBattlefields.get(player2.getId()).add(enchanted);
        gd.playerBattlefields.get(player2.getId()).add(other);

        Permanent aura = new Permanent(new Grounded());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new Grounded()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent land = findPermanent(player1, "Plains");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
