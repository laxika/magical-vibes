package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AceFlockbringer.class, AirElemental.class, GrizzlyBears.class, Counterspell.class})
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
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castGrizzlyBears() {
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    @Test
    void stillConjuresTheDuplicateWhenTheCreatureSpellIsCountered() {
        addCreatureReady(player1, new AceFlockbringer());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).singleElement().satisfies(card -> {
            assertThat(card.getName()).isEqualTo("Grizzly Bears");
            assertThat(card.hasKeyword(Keyword.FLYING)).isTrue();
        });
    }

    @Test
    void castingAFlyingCreatureDoesNotUseTheOncePerTurnTrigger() {
        addCreatureReady(player1, new AceFlockbringer());
        harness.setHand(player1, List.of(new AirElemental(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        castGrizzlyBears();

        assertThat(gd.playerHands.get(player1.getId())).singleElement().satisfies(card -> {
            assertThat(card.getName()).isEqualTo("Grizzly Bears");
            assertThat(card.hasKeyword(Keyword.FLYING)).isTrue();
        });
    }

    @Test
    void anOpponentsCreatureSpellDoesNotTriggerAce() {
        addCreatureReady(player1, new AceFlockbringer());
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void conjuredDuplicateRetainsFlyingWhenCastButTheOriginalDoesNotGainIt() {
        addCreatureReady(player1, new AceFlockbringer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        castGrizzlyBears();
        castGrizzlyBears();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(gqs.hasKeyword(gd, findPermanents(player1, "Grizzly Bears").getFirst(), Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanents(player1, "Grizzly Bears").getLast(), Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canTriggerAgainOnALaterTurn() {
        addCreatureReady(player1, new AceFlockbringer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        castGrizzlyBears();

        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        castGrizzlyBears();

        assertThat(gd.playerHands.get(player1.getId())).singleElement().satisfies(card -> {
            assertThat(card.getName()).isEqualTo("Grizzly Bears");
            assertThat(card.hasKeyword(Keyword.FLYING)).isTrue();
        });
    }

    @Test
    void eachAceConjuresItsOwnDuplicate() {
        addCreatureReady(player1, new AceFlockbringer());
        addCreatureReady(player1, new AceFlockbringer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        castGrizzlyBears();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).allSatisfy(card -> {
            assertThat(card.getName()).isEqualTo("Grizzly Bears");
            assertThat(card.hasKeyword(Keyword.FLYING)).isTrue();
        });
    }
}
