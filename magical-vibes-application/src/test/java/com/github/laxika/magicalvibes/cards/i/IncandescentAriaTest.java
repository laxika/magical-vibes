package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IncandescentAria.class, AirElemental.class, GiantSpider.class})
class IncandescentAriaTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each nontoken creature on both battlefields")
    void damagesEachNontokenCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castIncandescentAria();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not damage creature tokens")
    void doesNotDamageCreatureTokens() {
        Permanent ownToken = addTokenCreature(player1, "Soldier Token");
        Permanent opponentToken = addTokenCreature(player2, "Zombie Token");

        castIncandescentAria();

        assertThat(ownToken.getMarkedDamage()).isZero();
        assertThat(opponentToken.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Soldier Token");
        harness.assertOnBattlefield(player2, "Zombie Token");
    }

    private void castIncandescentAria() {
        harness.setHand(player1, List.of(new IncandescentAria()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent addTokenCreature(Player player, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(4);
        card.setToughness(4);
        card.setToken(true);

        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
