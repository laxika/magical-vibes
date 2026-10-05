package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.t.TheFirstEruption;
import com.github.laxika.magicalvibes.cards.w.WordOfSeizing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NarciFableSinger.class, Narcissism.class, TheFirstEruption.class,
        DeepFreeze.class, WordOfSeizing.class})
class NarciFableSingerTest extends BaseCardTest {

    @Test
    void drawsWhenYouSacrificeAnEnchantment() {
        harness.addToBattlefield(player1, new NarciFableSinger());
        harness.addToBattlefield(player1, new Narcissism());
        harness.setLibrary(player1, List.of(new TheFirstEruption()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, 1, null, findPermanent(player1, "Narci, Fable Singer").getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Narcissism");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "The First Eruption");
        harness.assertNotOnBattlefield(player1, "Narcissism");
    }

    @Test
    void finalChapterUsesSagaManaValueForLifeLossAndGain() {
        harness.addToBattlefield(player1, new NarciFableSinger());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstEruption());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new Narcissism()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void combatDamageGainsLifeThroughLifelink() {
        addCreatureReady(player1, new NarciFableSinger());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void opponentsEnchantmentSacrificeDoesNotDrawForYou() {
        harness.addToBattlefield(player1, new NarciFableSinger());
        harness.addToBattlefield(player2, new Narcissism());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NarciFableSinger());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheFirstEruption()));
        harness.setLibrary(player2, List.of(new TheFirstEruption()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player2, 0, 1, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInHand(player2, "The First Eruption");
        harness.assertInGraveyard(player2, "Narcissism");
    }

    @Test
    void sacrificingSagaAfterFinalChapterAlsoDrawsACard() {
        harness.addToBattlefield(player1, new NarciFableSinger());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstEruption());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Narcissism()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "The First Eruption");
        harness.assertNotOnBattlefield(player1, "The First Eruption");
        harness.assertInHand(player1, "Narcissism");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void nonfinalChapterDoesNotDrainLife() {
        harness.addToBattlefield(player1, new NarciFableSinger());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstEruption());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "The First Eruption");
    }

    @Test
    void losingAbilitiesPreventsSacrificeDrawTrigger() {
        Permanent narci = harness.addToBattlefieldAndReturn(player1, new NarciFableSinger());
        harness.addToBattlefield(player1, new Narcissism());
        Permanent freeze = harness.addToBattlefieldAndReturn(player2, new DeepFreeze());
        freeze.setAttachedTo(narci.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheFirstEruption()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, 1, null, narci.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Narcissism");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void finalChapterChecksSagaControllerAtResolution() {
        harness.addToBattlefield(player2, new NarciFableSinger());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstEruption());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setHand(player2, List.of(new WordOfSeizing()));
        harness.setLibrary(player2, List.of(new Narcissism()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.addMana(player2, ManaColor.RED, 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);

        harness.castInstant(player2, 0, saga.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "The First Eruption");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }
}
