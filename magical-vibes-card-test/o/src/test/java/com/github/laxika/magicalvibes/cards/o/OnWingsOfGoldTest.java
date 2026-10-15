package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZombieBrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnWingsOfGold.class, Disentomb.class, GrizzlyBears.class, ZombieBrute.class})
class OnWingsOfGoldTest extends BaseCardTest {

    @Test
    void boostsZombiesAndTokensAndGivesThemFlying() {
        Permanent zombie = addCreatureReady(player1, new ZombieBrute());
        Permanent token = new Permanent(tokenCard());
        gd.playerBattlefields.get(player1.getId()).add(token);
        Permanent ordinaryCreature = addCreatureReady(player1, new GrizzlyBears());
        int zombiePower = gqs.getEffectivePower(gd, zombie);
        int zombieToughness = gqs.getEffectiveToughness(gd, zombie);
        harness.addToBattlefield(player1, new OnWingsOfGold());

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(zombiePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(zombieToughness + 1);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ordinaryCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ordinaryCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    void createsWhiteZombieWhenCardsLeaveYourGraveyard() {
        harness.addToBattlefield(player1, new OnWingsOfGold());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    private Card tokenCard() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
