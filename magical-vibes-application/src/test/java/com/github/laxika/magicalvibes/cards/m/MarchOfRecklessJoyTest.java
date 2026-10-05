package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchOfRecklessJoy.class, Shock.class, SavannahLions.class,
        GrizzlyBears.class, HillGiant.class, Forest.class})
class MarchOfRecklessJoyTest extends BaseCardTest {

    @Test
    void exilingRedCardsFromHandReducesGenericCost() {
        Shock firstRedCard = new Shock();
        Shock secondRedCard = new Shock();
        Shock libraryCard = new Shock();
        harness.setHand(player1, List.of(new MarchOfRecklessJoy(), firstRedCard, secondRedCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(firstRedCard, secondRedCard, libraryCard);
    }

    @Test
    void allowsPlayingUpToTwoExiledCards() {
        SavannahLions firstCard = new SavannahLions();
        GrizzlyBears secondCard = new GrizzlyBears();
        HillGiant thirdCard = new HillGiant();
        harness.setHand(player1, List.of(new MarchOfRecklessJoy()));
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantForX(player1, 0, 3, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, firstCard.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, secondCard.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, thirdCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .containsExactly(firstCard, secondCard);
        assertThat(gd.findExiledCard(thirdCard.getId())).isNotNull();
    }

    @Test
    void reductionDoesNotReduceChosenXOrGrantPermissionForCostCards() {
        Shock costCard = new Shock();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setHand(player1, List.of(new MarchOfRecklessJoy(), costCard));
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(costCard, first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, costCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
    }

    @Test
    void zeroXExilesNothingAndLeavesUnselectedHandCards() {
        MarchOfRecklessJoy unselected = new MarchOfRecklessJoy();
        Forest topCard = new Forest();
        harness.setHand(player1, List.of(new MarchOfRecklessJoy(), unselected));
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(unselected);
    }

    @Test
    void cannotExileNonredCardsToPayTheAdditionalCost() {
        harness.setHand(player1, List.of(new MarchOfRecklessJoy(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 2, List.of(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void exilingRedCardsCannotPayTheRequiredRedMana() {
        harness.setHand(player1, List.of(new MarchOfRecklessJoy(), new MarchOfRecklessJoy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 2, List.of(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void landsUseTheSharedTwoCardLimitAcrossTurns() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setHand(player1, List.of(new MarchOfRecklessJoy()));
        harness.setLibrary(player1, List.of(first, second, third, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstantForX(player1, 0, 3, List.of());
        harness.passBothPriorities();

        harness.castFromExile(player1, first.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player2, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, second.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, third.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard()).containsExactly(first, second);
        assertThat(gd.findExiledCard(third.getId())).isNotNull();
    }

    @Test
    void unusedPermissionExpiresAfterTheEndOfYourNextTurn() {
        Forest exiled = new Forest();
        harness.setHand(player1, List.of(new MarchOfRecklessJoy()));
        harness.setLibrary(player1, List.of(exiled, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForX(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    @Test
    void canCastAnExiledMarchWithoutPayingItsOptionalAdditionalCost() {
        MarchOfRecklessJoy exiledMarch = new MarchOfRecklessJoy();
        harness.setHand(player1, List.of(new MarchOfRecklessJoy()));
        harness.setLibrary(player1, List.of(exiledMarch));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForX(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, exiledMarch.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledMarch.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(exiledMarch);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
