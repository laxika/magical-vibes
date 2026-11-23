package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.a.AinokTracker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeginAnew.class, GrizzlyBears.class, Unsummon.class, AinokTracker.class})
class BeginAnewTest extends BaseCardTest {

    @Test
    void destroysAllCreaturesAndPerpetuallyBoostsAllCreatureCardsInHand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GrizzlyBears firstBear = new GrizzlyBears();
        GrizzlyBears secondBear = new GrizzlyBears();
        harness.setHand(player1, List.of(new BeginAnew(), firstBear, secondBear));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> creatures = gd.playerBattlefields.get(player1.getId());
        assertThat(creatures).hasSize(2);
        assertThat(creatures).allSatisfy(permanent -> {
            assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(3);
        });
    }

    @Test
    void perpetualBonusAppliesWhileCreatureCardIsStillInHand() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setHand(player1, List.of(new BeginAnew(), bear));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveCardPower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    void repeatedResolutionsStackBonusesEvenWithNoCreaturesOnBattlefield() {
        harness.setHand(player1, List.of(new BeginAnew(), new BeginAnew(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    void doesNotBoostCreatureCardsInOpponentsHand() {
        harness.setHand(player1, List.of(new BeginAnew()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    void bonusPersistsWhenCreatureReturnsToHandAndIsCastAgain() {
        harness.setHand(player1, List.of(new BeginAnew(), new GrizzlyBears(), new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, findPermanent(player1, "Grizzly Bears").getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("A boosted creature card cast face down is a 2/2 until it is turned face up")
    void boostedCardCastFaceDownIsTwoTwoUntilTurnedFaceUp() {
        harness.setHand(player1, List.of(new BeginAnew(), new AinokTracker()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent tracker = findPermanent(player1, "Ainok Tracker");
        assertThat(tracker.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(2);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tracker));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(4);
    }
}
