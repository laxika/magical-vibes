package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldenSidekick.class, AngelOfMercy.class, GrizzlyBears.class, Plains.class})
class GoldenSidekickTest extends BaseCardTest {

    @Test
    void lifeGainPerpetuallyBoostsARandomCreatureCardInHand() {
        harness.addToBattlefield(player1, new GoldenSidekick());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 2);
        resolveAllTriggers();

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand.stream().filter(card -> card.getPower() == 5 && card.getToughness() == 5).count())
                .isEqualTo(1);
        assertThat(hand.stream().filter(card -> card.getPower() == 2 && card.getToughness() == 2).count())
                .isEqualTo(1);
    }

    @Test
    void repeatedLifeGainsAccumulateAndBoostPersistsOntoBattlefield() {
        harness.addToBattlefield(player1, new GoldenSidekick());
        harness.setHand(player1, List.of(new GrizzlyBears(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 1);
        resolveAllTriggers();

        harness.setHand(player1, List.of(gd.playerHands.get(player1.getId()).getFirst(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 1);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        var bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(8);
    }

    @Test
    void noncreatureCardsAndOpponentsHandAreNotEligible() {
        harness.addToBattlefield(player1, new GoldenSidekick());
        harness.setHand(player1, List.of(new Plains(), new GrizzlyBears(), new AngelOfMercy()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 2);
        resolveAllTriggers();

        Card boosted = gd.playerHands.get(player1.getId()).get(1);
        assertThat(boosted.getPower()).isEqualTo(5);
        assertThat(boosted.getToughness()).isEqualTo(5);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getPower()).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getToughness()).isEqualTo(2);
    }

    @Test
    void opponentsLifeGainDoesNotBoostControllersHand() {
        harness.addToBattlefield(player2, new GoldenSidekick());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Card unboosted = gd.playerHands.get(player2.getId()).getFirst();
        assertThat(unboosted.getPower()).isEqualTo(2);
        assertThat(unboosted.getToughness()).isEqualTo(2);
    }

    @Test
    void lifeGainWithNoCreatureCardInHandResolvesWithoutAChoice() {
        harness.addToBattlefield(player1, new GoldenSidekick());
        harness.setHand(player1, List.of(new Plains(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void selectsFromHandAtResolutionEvenIfHandWasEmptyWhenLifeWasGained() {
        harness.addToBattlefield(player1, new GoldenSidekick());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        resolveAllTriggers();

        Card boosted = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(boosted.getPower()).isEqualTo(5);
        assertThat(boosted.getToughness()).isEqualTo(5);
    }

    @Test
    void ownLifelinkDamageBoostsCreatureCardByLifeGained() {
        addCreatureReady(player1, new GoldenSidekick());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        Card boosted = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(boosted.getPower()).isEqualTo(3);
        assertThat(boosted.getToughness()).isEqualTo(3);
    }
}
