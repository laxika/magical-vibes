package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PenumbraBobcat.class, WrathOfGod.class})
class PenumbraBobcatTest extends BaseCardTest {

    @Test
    void deathCreatesBlackCatToken() {
        harness.addToBattlefield(player1, new PenumbraBobcat());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Cat");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.CAT);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    void eachBobcatCreatesATokenWhenTheyDieTogether() {
        harness.addToBattlefield(player1, new PenumbraBobcat());
        harness.addToBattlefield(player1, new PenumbraBobcat());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Cat")).hasSize(2);
    }

    @Test
    void eachControllerReceivesTheirOwnTokenAfterSimultaneousDeaths() {
        harness.addToBattlefield(player1, new PenumbraBobcat());
        harness.addToBattlefield(player2, new PenumbraBobcat());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Cat")).hasSize(1);
        assertThat(findPermanents(player2, "Cat")).hasSize(1);
        harness.assertInGraveyard(player1, "Penumbra Bobcat");
        harness.assertInGraveyard(player2, "Penumbra Bobcat");
    }

    @Test
    void catTokenDoesNotCreateAnotherTokenWhenItDies() {
        harness.addToBattlefield(player1, new PenumbraBobcat());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Cat")).hasSize(1);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Cat")).isEmpty();
        assertThat(findPermanents(player2, "Cat")).isEmpty();
    }
}
