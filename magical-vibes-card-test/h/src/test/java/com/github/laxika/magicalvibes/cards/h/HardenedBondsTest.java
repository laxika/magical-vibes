package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BondBeetle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HardenedBonds.class, BondBeetle.class, Forest.class, GrizzlyBears.class})
class HardenedBondsTest extends BaseCardTest {

    @Test
    void seeksCreatureAndPerpetuallyBoostsTheSoughtCard() {
        harness.addToBattlefield(player1, new HardenedBonds());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears sought = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, sought));
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent boosted = findPermanentByCard(sought);
        assertThat(gqs.getEffectivePower(gd, boosted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boosted)).isEqualTo(3);
    }

    @Test
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new HardenedBonds());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears firstSought = new GrizzlyBears();
        GrizzlyBears secondSought = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstSought, secondSought));
        harness.setHand(player1, List.of(new BondBeetle(), new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, List.of(target.getId()));
        resolveAllTriggers();
        harness.castCreature(player1, 0, List.of(target.getId()));
        resolveAllTriggers();

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        Card sought = hand.get(0);
        assertThat(List.<Card>of(firstSought, secondSought)).contains(sought);
        Card remaining = sought.getId().equals(firstSought.getId()) ? secondSought : firstSought;
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    private Permanent findPermanentByCard(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
