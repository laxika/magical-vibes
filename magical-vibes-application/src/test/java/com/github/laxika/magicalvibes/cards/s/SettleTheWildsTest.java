package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SettleTheWilds.class, Forest.class, ElvishMystic.class, GrizzlyBears.class, GiantGrowth.class})
class SettleTheWildsTest extends BaseCardTest {

    @Test
    void seeksTappedBasicLandThenSeeksExactLandCountPermanentToHand() {
        Card forest = new Forest();
        Card mystic = new ElvishMystic();
        Card nonPermanent = new GiantGrowth();
        harness.setHand(player1, List.of(new SettleTheWilds()));
        harness.setLibrary(player1, List.of(forest, mystic, nonPermanent));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent enteredForest = findPermanent(player1, "Forest");
        assertThat(enteredForest.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(mystic);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonPermanent);
    }

    @Test
    void doesNotSeekPermanentWithDifferentManaValue() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new SettleTheWilds()));
        harness.setLibrary(player1, List.of(forest, bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }
}
