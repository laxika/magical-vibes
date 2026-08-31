package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildfireHowl.class, GrizzlyBears.class, HillGiant.class})
class WildfireHowlTest extends BaseCardTest {

    @Test
    void withoutGiftDealsTwoDamageToEachCreatureAndDoesNotMakeOpponentDraw() {
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        Card drawCard = new GrizzlyBears();
        harness.addToBattlefield(player1, ownCreature);
        harness.addToBattlefield(player2, opposingCreature);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawCard));

        castWithoutGift();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void withGiftOpponentDrawsAndSpellAlsoDealsOneDamageToAnyTarget() {
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new HillGiant();
        Card drawCard = new GrizzlyBears();
        harness.addToBattlefield(player1, ownCreature);
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, opposingCreature);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawCard));

        castWithGift(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(targetCreature.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(drawCard.getId());
    }

    private void castWithoutGift() {
        prepareSpell();
        harness.castSorceryWithGift(player1, 0, List.of(), false);
        harness.passBothPriorities();
    }

    private void castWithGift(java.util.UUID targetId) {
        prepareSpell();
        harness.castSorceryWithGift(player1, 0, targetId, true);
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new WildfireHowl()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
