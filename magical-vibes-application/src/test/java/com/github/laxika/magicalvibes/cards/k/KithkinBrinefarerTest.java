package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.ThoughtweftsCall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KithkinBrinefarer.class, ThoughtweftsCall.class, KithkinBillyrider.class, GrizzlyBears.class})
class KithkinBrinefarerTest extends BaseCardTest {

    @Test
    void conjuresDuplicateWhenAKithkinIsPutIntoHandFromLibrary() {
        harness.addToBattlefield(player1, new KithkinBrinefarer());
        Card kithkin = new KithkinBillyrider();
        harness.setLibrary(player1, List.of(kithkin));
        harness.setHand(player1, List.of(new ThoughtweftsCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Kithkin Billyrider"))
                .hasSize(2)
                .anyMatch(card -> !card.getId().equals(kithkin.getId()));
    }

    @Test
    void attackingPerpetuallyBoostsKithkinCreatureCardsInHandOnly() {
        Card kithkin = new KithkinBillyrider();
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(kithkin, bears));
        addCreatureReady(player1, new KithkinBrinefarer());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.perpetualPowerToughnessModifiers).containsKey(kithkin.getId());
        assertThat(gd.perpetualPowerToughnessModifiers).doesNotContainKey(bears.getId());
    }
}
