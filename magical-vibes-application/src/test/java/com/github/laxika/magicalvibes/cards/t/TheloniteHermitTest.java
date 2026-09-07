package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheloniteHermit.class, GrizzlyBears.class})
class TheloniteHermitTest extends BaseCardTest {

    @Test
    void boostsSaprolingsRegardlessOfController() {
        harness.addToBattlefield(player1, new TheloniteHermit());
        Permanent ownSaproling = harness.addToBattlefieldAndReturn(player1, createSaproling());
        Permanent opposingSaproling = harness.addToBattlefieldAndReturn(player2, createSaproling());
        Permanent unrelatedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, unrelatedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unrelatedCreature)).isEqualTo(2);
    }

    @Test
    void turningFaceUpCreatesFourBoostedSaprolings() {
        harness.setHand(player1, List.of(new TheloniteHermit()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent hermit = findPermanent(player1, "Thelonite Hermit");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hermit));
        harness.passBothPriorities();

        assertThat(hermit.isFaceDown()).isFalse();
        List<Permanent> saprolings = findPermanents(player1, "Saproling");
        assertThat(saprolings).hasSize(4).allSatisfy(saproling -> {
            assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
            assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(2);
        });
    }

    private Card createSaproling() {
        Card card = new Card();
        card.setName("Saproling");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setSubtypes(List.of(CardSubtype.SAPROLING));
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
