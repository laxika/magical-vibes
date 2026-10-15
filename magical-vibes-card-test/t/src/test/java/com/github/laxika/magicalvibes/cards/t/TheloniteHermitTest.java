package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Sprout;
import com.github.laxika.magicalvibes.cards.s.SuddenShock;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
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

@CardUsed({TheloniteHermit.class, GrizzlyBears.class, Sprout.class, SuddenShock.class,
        WingsOfVelisVel.class})
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

    @Test
    void castingFaceUpDoesNotCreateSaprolings() {
        harness.setHand(player1, List.of(new TheloniteHermit()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Thelonite Hermit").isFaceDown()).isFalse();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    void faceDownHermitDoesNotBoostExistingSaprolings() {
        harness.setHand(player1, List.of(new Sprout(), new TheloniteHermit()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);
        Permanent saproling = findPermanent(player1, "Saproling");

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Thelonite Hermit").isFaceDown()).isTrue();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(1);
    }

    @Test
    void turnFaceUpTriggerCreatesTokensEvenIfHermitDiesBeforeResolution() {
        harness.setHand(player1, List.of(new TheloniteHermit()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent hermit = findPermanent(player1, "Thelonite Hermit");

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hermit));
        assertThat(findPermanents(player1, "Saproling")).isEmpty();

        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, hermit.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thelonite Hermit")).isEmpty();
        assertThat(findPermanents(player1, "Saproling")).hasSize(4).allSatisfy(saproling -> {
            assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(1);
        });
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    void boostsItselfWhenItBecomesASaproling() {
        Permanent hermit = harness.addToBattlefieldAndReturn(player1, new TheloniteHermit());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, hermit.getId());

        assertThat(gqs.getEffectivePower(gd, hermit)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hermit)).isEqualTo(5);
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
