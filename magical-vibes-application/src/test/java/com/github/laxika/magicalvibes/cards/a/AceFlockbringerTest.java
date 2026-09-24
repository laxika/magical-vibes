package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AceFlockbringer.class, AirElemental.class, GrizzlyBears.class})
class AceFlockbringerTest extends BaseCardTest {

    @Test
    void conjuresOneFlyingDuplicateOfTheFirstNonFlyingCreatureSpellEachTurn() {
        addCreatureReady(player1, new AceFlockbringer());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        castGrizzlyBears();
        castGrizzlyBears();

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(hand.getFirst().hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotTriggerForACreatureSpellWithFlying() {
        addCreatureReady(player1, new AceFlockbringer());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castGrizzlyBears() {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
