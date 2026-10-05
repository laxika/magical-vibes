package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BondBeetle;
import com.github.laxika.magicalvibes.cards.h.HardenedBonds;
import com.github.laxika.magicalvibes.cards.t.TopsoilTurner;
import com.github.laxika.magicalvibes.cards.y.ThoughtweftsCall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PerpetualPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KithkinBrinefarer.class, ThoughtweftsCall.class, TopsoilTurner.class, HardenedBonds.class, BondBeetle.class})
class KithkinBrinefarerTest extends BaseCardTest {

    @Test
    void conjuresDuplicateWhenAKithkinIsPutIntoHandFromLibrary() {
        harness.addToBattlefield(player1, new KithkinBrinefarer());
        Card kithkin = new KithkinBrinefarer();
        harness.setLibrary(player1, List.of(kithkin));
        harness.setHand(player1, List.of(new ThoughtweftsCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Kithkin Brinefarer"))
                .hasSize(2)
                .anyMatch(card -> !card.getId().equals(kithkin.getId()));
    }

    @Test
    void attackingPerpetuallyBoostsKithkinCreatureCardsInHandOnly() {
        Card kithkin = new KithkinBrinefarer();
        Card nonKithkin = new TopsoilTurner();
        harness.setHand(player1, List.of(kithkin, nonKithkin));
        addCreatureReady(player1, new KithkinBrinefarer());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.perpetualPowerToughnessModifiers).containsKey(kithkin.getId());
        assertThat(gd.perpetualPowerToughnessModifiers).doesNotContainKey(nonKithkin.getId());
    }

    @Test
    void triggersWhenAnotherCreatureAttacksWhileBrinefarerStaysBack() {
        harness.addToBattlefield(player1, new KithkinBrinefarer());
        addCreatureReady(player1, new TopsoilTurner());
        Card kithkin = new KithkinBrinefarer();
        harness.setHand(player1, List.of(kithkin));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.perpetualPowerToughnessModifiers.get(kithkin.getId()))
                .isEqualTo(new PerpetualPowerToughnessModifier(1, 1));
    }

    @Test
    void attackingWithMultipleCreaturesBoostsOnceAndOnlyYourHand() {
        addCreatureReady(player1, new KithkinBrinefarer());
        addCreatureReady(player1, new TopsoilTurner());
        Card kithkin = new KithkinBrinefarer();
        Card opposingKithkin = new KithkinBrinefarer();
        harness.setHand(player1, List.of(kithkin));
        harness.setHand(player2, List.of(opposingKithkin));

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.perpetualPowerToughnessModifiers.get(kithkin.getId()))
                .isEqualTo(new PerpetualPowerToughnessModifier(1, 1));
        assertThat(gd.perpetualPowerToughnessModifiers).doesNotContainKey(opposingKithkin.getId());
    }

    @Test
    void drawingMultipleKithkinCardsConjuresOneDuplicateOfEach() {
        harness.addToBattlefield(player1, new KithkinBrinefarer());
        Card first = new KithkinBrinefarer();
        Card second = new KithkinBrinefarer();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDuplicateNonKithkinOrOpponentsDraws() {
        harness.addToBattlefield(player1, new KithkinBrinefarer());
        Card nonKithkin = new TopsoilTurner();
        Card opposingKithkin = new KithkinBrinefarer();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(nonKithkin));
        harness.setLibrary(player2, List.of(opposingKithkin));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonKithkin);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opposingKithkin);
    }

    @Test
    void perpetualBoostRemainsWhenTheKithkinIsCast() {
        addCreatureReady(player1, new KithkinBrinefarer());
        Card kithkin = new KithkinBrinefarer();
        harness.setHand(player1, List.of(kithkin));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, kithkin, "{1}{G}{W}");
        resolveAllTriggers();

        var permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(kithkin.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(4);
    }

    @Test
    @CardUsed({HardenedBonds.class, BondBeetle.class})
    void conjuresDuplicateWhenHardenedBondsSeeksAKithkin() {
        var brinefarer = harness.addToBattlefieldAndReturn(player1, new KithkinBrinefarer());
        harness.addToBattlefield(player1, new HardenedBonds());
        Card sought = new KithkinBrinefarer();
        harness.setLibrary(player1, List.of(sought));
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, List.of(brinefarer.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

}
