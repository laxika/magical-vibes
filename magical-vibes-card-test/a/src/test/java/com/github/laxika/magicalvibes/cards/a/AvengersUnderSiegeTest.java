package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BaronStruckerHYDRAOverlord;
import com.github.laxika.magicalvibes.cards.d.DocOckSinisterScientist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvengersUnderSiege.class, BaronStruckerHYDRAOverlord.class,
        DocOckSinisterScientist.class, GrizzlyBears.class})
class AvengersUnderSiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates two 2/1 black Villain tokens with menace")
    void chapterICreatesVillains() {
        harness.setHand(player1, List.of(new AvengersUnderSiege()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Villain").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VILLAIN);
            assertThat(token.hasKeyword(Keyword.MENACE)).isTrue();
        });
    }

    @Test
    @DisplayName("Chapter II damages non-Villain creatures and each opponent")
    void chapterIIDamagesNonVillainsAndOpponents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent villain = harness.addToBattlefieldAndReturn(player2, new DocOckSinisterScientist());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AvengersUnderSiege());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).contains(villain);
    }

    @Test
    @DisplayName("Chapter III creates a Treasure for each Villain controlled")
    void chapterIIICreatesTreasureForEachVillain() {
        harness.addToBattlefield(player1, new DocOckSinisterScientist());
        harness.addToBattlefield(player1, new BaronStruckerHYDRAOverlord());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AvengersUnderSiege());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player1, "Avengers: Under Siege")).isEmpty();
    }

    @Test
    @DisplayName("Chapter II leaves low-toughness Villains on both sides undamaged")
    void chapterIISparesVillainsOnBothSides() {
        Permanent ownVillain = harness.addToBattlefieldAndReturn(player1, new BaronStruckerHYDRAOverlord());
        Permanent opposingVillain = harness.addToBattlefieldAndReturn(player2, new BaronStruckerHYDRAOverlord());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AvengersUnderSiege());
        saga.setCounterCount(CounterType.LORE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).contains(ownVillain);
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).contains(opposingVillain);
        assertThat(ownVillain.getMarkedDamage()).isZero();
        assertThat(opposingVillain.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Chapter III creates no Treasure for opposing Villains")
    void chapterIIIDoesNotCountOpposingVillains() {
        harness.addToBattlefield(player2, new BaronStruckerHYDRAOverlord());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AvengersUnderSiege());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Avengers: Under Siege");
    }

    @Test
    @DisplayName("Chapter III counts Villains at resolution and keeps the Saga until then")
    void chapterIIICountsVillainsAtResolution() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AvengersUnderSiege());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avengers: Under Siege");
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.addToBattlefield(player1, new BaronStruckerHYDRAOverlord());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).allSatisfy(treasure ->
                assertThat(treasure.isTapped()).isFalse());
        harness.assertInGraveyard(player1, "Avengers: Under Siege");
    }
}
