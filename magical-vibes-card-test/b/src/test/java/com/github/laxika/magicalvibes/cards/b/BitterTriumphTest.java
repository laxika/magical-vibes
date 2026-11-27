package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FutureSight;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BitterTriumph.class, FutureSight.class, Forest.class, QuintoriusKand.class, ArmoredKincaller.class, Plains.class})
class BitterTriumphTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card and destroys a target creature")
    void discardsCardAndDestroysCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredKincaller());
        harness.setHand(player1, List.of(new BitterTriumph(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Armored Kincaller");
    }

    @Test
    @DisplayName("Cast from the top of the library with an empty hand, pays 3 life")
    void castFromLibraryTopWithAnEmptyHandPaysLife() {
        harness.addToBattlefield(player1, new FutureSight());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredKincaller());
        harness.setLibrary(player1, List.of(new BitterTriumph()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player2, "Armored Kincaller");
    }

    @Test
    @DisplayName("Cast from the top of the library with cards in hand, discards the announced card")
    void castFromLibraryTopDiscardsTheAnnouncedCard() {
        harness.addToBattlefield(player1, new FutureSight());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredKincaller());
        Forest kept = new Forest();
        Plains discarded = new Plains();
        harness.setLibrary(player1, List.of(new BitterTriumph()));
        harness.setHand(player1, List.of(kept, discarded));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCardFromLibraryTop(gd, player1, null, target.getId(), List.of(), List.of(), false, false, null, 1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        harness.assertInGraveyard(player2, "Armored Kincaller");
    }

    @Test
    @DisplayName("Cast from the top of the library with cards in hand and no discard named, pays 3 life")
    void castFromLibraryTopWithCardsInHandMayPayLife() {
        harness.addToBattlefield(player1, new FutureSight());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredKincaller());
        harness.setLibrary(player1, List.of(new BitterTriumph()));
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Armored Kincaller");
    }

    @Test
    @DisplayName("Pays 3 life and destroys a target planeswalker")
    void paysLifeAndDestroysPlaneswalker() {
        Permanent target = addReadyPlaneswalker(player2, 4);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BitterTriumph()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithDiscard(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player2, "Quintorius Kand");
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker permanent")
    void rejectsLandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new BitterTriumph(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new QuintoriusKand());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }

    @Test
    void discardIsPaidBeforeResolutionWithoutLosingLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredKincaller());
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of(new Forest(), new BitterTriumph()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithDiscard(player1, 1, target.getId(), 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player1, 2);
        harness.assertOnBattlefield(player2, "Armored Kincaller");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Armored Kincaller");
        harness.assertLife(player1, 2);
    }

    @Test
    void mayPayLifeEvenWhenAnotherCardCouldBeDiscarded() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BitterTriumph(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithDiscard(player1, 0, target.getId(), null);

        harness.assertLife(player1, 17);
        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Armored Kincaller");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Armored Kincaller");
        harness.assertLife(player1, 17);
    }

    @Test
    void cannotPayThreeLifeWithOnlyTwoLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredKincaller());
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of(new BitterTriumph()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 2);
        harness.assertOnBattlefield(player2, "Armored Kincaller");
    }

    @Test
    void cannotDiscardTheSpellItselfToPayItsCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredKincaller());
        harness.setHand(player1, List.of(new BitterTriumph(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player2, "Armored Kincaller");
    }
}
