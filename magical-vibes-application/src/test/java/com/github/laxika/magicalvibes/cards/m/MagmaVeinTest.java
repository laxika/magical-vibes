package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MagmaVeinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land deals 1 damage to each creature without flying")
    void sacrificingLandDealsDamageOnlyToCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new MagmaVein());
        harness.addToBattlefield(player1, new Forest());
        Permanent groundCreature = addCreature(player2, creature("Ground creature", false));
        Permanent flyingCreature = addCreature(player2, creature("Flying creature", true));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(groundCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest);
    }

    @Test
    @DisplayName("Magma Vein cannot be activated without a land to sacrifice")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new MagmaVein());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private static Card creature(String name, boolean flying) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        card.setPower(2);
        card.setToughness(2);
        if (flying) {
            card.setKeywords(Set.of(Keyword.FLYING));
        }
        return card;
    }
}
