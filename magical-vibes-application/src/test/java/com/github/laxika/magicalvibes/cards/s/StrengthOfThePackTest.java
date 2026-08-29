package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StrengthOfThePackTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on each creature you control")
    void putsCountersOnControlledCreatures() {
        Permanent ownCreature = addPermanent(player1, new GrizzlyBears());
        Permanent ownCreatureTwo = addPermanent(player1, new GrizzlyBears());
        Permanent opposingCreature = addPermanent(player2, new GrizzlyBears());
        addPermanent(player1, noncreature());

        harness.setHand(player1, List.of(new StrengthOfThePack()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownCreatureTwo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Relic")
                        && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 0);
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Card noncreature() {
        Card card = new Card();
        card.setName("Relic");
        card.setType(CardType.ARTIFACT);
        return card;
    }
}
