package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Malanthrope.class, GrizzlyBears.class, Forest.class})
class MalanthropeTest extends BaseCardTest {

    @Test
    void exilesTargetPlayersGraveyardAndGetsCountersForCreatureCards() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card noncreature = new Forest();
        harness.setGraveyard(player2, List.of(firstCreature, secondCreature, noncreature));

        Permanent malanthrope = castMalanthrope(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(firstCreature, secondCreature, noncreature);
        assertThat(malanthrope.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void exilesNoncreatureCardsEvenWhenNoCreatureCardsArePresent() {
        Card firstNoncreature = new Forest();
        Card secondNoncreature = new Forest();
        harness.setGraveyard(player2, List.of(firstNoncreature, secondNoncreature));

        Permanent malanthrope = castMalanthrope(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(firstNoncreature, secondNoncreature);
        assertThat(malanthrope.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castMalanthrope(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Malanthrope()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, targetPlayerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Malanthrope");
    }
}
