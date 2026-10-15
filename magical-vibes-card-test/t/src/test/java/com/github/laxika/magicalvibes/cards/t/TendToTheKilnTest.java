package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Flamebraider;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({TendToTheKiln.class, Flamebraider.class, LightningBolt.class, Divination.class, Stifle.class})
class TendToTheKilnTest extends BaseCardTest {

    @Test
    @DisplayName("Non-Elemental instants in hand become Elementals and fuel flame counters")
    void convertsOwnedInstantAndSorceryCards() {
        harness.setHand(player1, List.of(new LightningBolt()));
        Permanent kiln = harness.enterBattlefieldAndReturn(player1, new TendToTheKiln());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(kiln.getCounterCount(CounterType.FLAME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Three Elemental spells conjure a hasty Flamebraider and remove the counters")
    void conjuresFlamebraiderAtThreeCounters() {
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        Permanent kiln = harness.enterBattlefieldAndReturn(player1, new TendToTheKiln());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 3);

        castAndResolveBolt();
        castAndResolveBolt();
        castAndResolveBolt();

        assertThat(kiln.getCounterCount(CounterType.FLAME)).isZero();
        Permanent flamebraider = findPermanent(player1, "Flamebraider");
        assertThat(flamebraider.getCard()).isInstanceOf(Flamebraider.class);
        assertThat(flamebraider.hasKeyword(Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Flamebraider")).isEmpty();
    }

    @Test
    void sorceriesBecomeElementalAndLibraryCardsRemainElementalAfterDrawing() {
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));
        Permanent kiln = harness.enterBattlefieldAndReturn(player1, new TendToTheKiln());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(kiln.getCounterCount(CounterType.FLAME)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.addMana(player1, ManaColor.RED, 1);
        castAndResolveBolt();
        assertThat(kiln.getCounterCount(CounterType.FLAME)).isEqualTo(2);
    }

    @Test
    void opposingElementalsDoNotTrigger() {
        harness.setHand(player2, List.of(new LightningBolt(), new Flamebraider()));
        Permanent kiln = harness.enterBattlefieldAndReturn(player1, new TendToTheKiln());
        resolveAllTriggers();
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(kiln.getCounterCount(CounterType.FLAME)).isZero();
        harness.assertLife(player1, 17);
    }

    @Test
    void cardsAcquiredAfterEntryAreNotConverted() {
        Permanent kiln = harness.enterBattlefieldAndReturn(player1, new TendToTheKiln());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(kiln.getCounterCount(CounterType.FLAME)).isZero();
        harness.assertLife(player2, 17);
    }

    @Test
    void nativeElementalTriggersAndRemovesAllFlameCountersButNoOtherCounters() {
        harness.setHand(player1, List.of(new Flamebraider()));
        Permanent kiln = harness.enterBattlefieldAndReturn(player1, new TendToTheKiln());
        resolveAllTriggers();
        kiln.setCounterCount(CounterType.FLAME, 4);
        kiln.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(kiln.getCounterCount(CounterType.FLAME)).isZero();
        assertThat(kiln.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Flamebraider")).hasSize(2);
    }

    @Test
    void conjuredCreatureIsSacrificedByItsNewController() {
        Permanent flamebraider = conjureFlamebraider();
        gd.playerBattlefields.get(player1.getId()).remove(flamebraider);
        gd.playerBattlefields.get(player2.getId()).add(flamebraider);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Flamebraider");
        harness.assertInGraveyard(player1, "Flamebraider");
    }

    @Test
    void sacrificeAbilityTriggersAgainAfterItsFirstTriggerIsCountered() {
        conjureFlamebraider();
        harness.setHand(player1, List.of(new Stifle()));
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, gd.stack.getLast().getTargetableId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Flamebraider");

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Flamebraider");
        harness.assertInGraveyard(player1, "Flamebraider");
    }

    @Test
    void hastePersistsAfterCleanupWhenConjuredDuringEndStep() {
        harness.setHand(player1, List.of(new LightningBolt()));
        Permanent kiln = harness.enterBattlefieldAndReturn(player1, new TendToTheKiln());
        resolveAllTriggers();
        kiln.setCounterCount(CounterType.FLAME, 2);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);
        castAndResolveBolt();
        Permanent flamebraider = findPermanent(player1, "Flamebraider");

        harness.passUntil(player2, TurnStep.UPKEEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Flamebraider");
        assertThat(gqs.hasKeyword(gd, flamebraider, Keyword.HASTE)).isTrue();
    }

    @Test
    void ownedCardsInGraveyardAndExileAreConvertedButOpposingCardsAreNot() {
        LightningBolt graveyardCard = new LightningBolt();
        LightningBolt exiledCard = new LightningBolt();
        LightningBolt opposingCard = new LightningBolt();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setExile(player1, List.of(exiledCard));
        harness.setHand(player2, List.of(opposingCard));
        Permanent kiln = harness.enterBattlefieldAndReturn(player1, new TendToTheKiln());
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(kiln);

        for (LightningBolt card : List.of(graveyardCard, exiledCard)) {
            assertThat(gqs.cardHasSubtype(card, CardSubtype.ELEMENTAL, gd, player1.getId())).isTrue();
            assertThat(gqs.cardHasType(card, CardType.KINDRED, gd, player1.getId())).isTrue();
            assertThat(gqs.cardHasType(card, CardType.INSTANT, gd, player1.getId())).isTrue();
        }
        assertThat(gqs.cardHasSubtype(opposingCard, CardSubtype.ELEMENTAL, gd, player2.getId())).isFalse();
        assertThat(gqs.cardHasType(opposingCard, CardType.KINDRED, gd, player2.getId())).isFalse();
    }

    private void castAndResolveBolt() {
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private Permanent conjureFlamebraider() {
        harness.setHand(player1, List.of(new LightningBolt()));
        Permanent kiln = harness.enterBattlefieldAndReturn(player1, new TendToTheKiln());
        resolveAllTriggers();
        kiln.setCounterCount(CounterType.FLAME, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        castAndResolveBolt();
        return findPermanent(player1, "Flamebraider");
    }
}
