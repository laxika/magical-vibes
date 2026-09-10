package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HamletVanguard.class)
class HamletVanguardTest extends BaseCardTest {

    @Test
    void entersWithTwoCountersForEachOtherNontokenHumanYouControl() {
        addHuman(player1, false);
        addHuman(player1, false);
        addHuman(player1, true);
        addHuman(player2, false);

        Permanent vanguard = castVanguard();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void entersWithoutCountersWhenYouControlNoNontokenHumans() {
        Permanent vanguard = castVanguard();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castVanguard() {
        harness.setHand(player1, List.of(new HamletVanguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Hamlet Vanguard");
    }

    private Permanent addHuman(com.github.laxika.magicalvibes.model.Player player, boolean token) {
        Card card = new Card();
        card.setName(token ? "Human Token" : "Human");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.HUMAN));
        card.setPower(1);
        card.setToughness(1);
        card.setToken(token);

        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
