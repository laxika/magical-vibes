package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({DihadaBinderOfWills.class, CaptainSisay.class, Forest.class, GrizzlyBears.class, RestInPeace.class})
class DihadaBinderOfWillsTest extends BaseCardTest {

    @Test
    @DisplayName("+2 grants the three keywords to a legendary creature")
    void plusTwoGrantsKeywordsToLegendaryCreatureYouControl() {
        Permanent dihada = addReadyDihada(player1, 5);
        Permanent sisay = harness.addToBattlefieldAndReturn(player1, new CaptainSisay());

        harness.activateAbility(player1, 0, 0, null, sisay.getId());
        harness.passBothPriorities();

        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("+2 cannot target a nonlegendary creature")
    void plusTwoRejectsNonlegendaryCreature() {
        addReadyDihada(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 puts legendary cards in hand, the rest in the graveyard, and creates Treasures")
    void minusThreePutsLegendaryCardsInHandAndCreatesTreasures() {
        addReadyDihada(player1, 5);
        CaptainSisay legendary = new CaptainSisay();
        Forest firstLand = new Forest();
        GrizzlyBears bear = new GrizzlyBears();
        Forest secondLand = new Forest();
        harness.setLibrary(player1, List.of(legendary, firstLand, bear, secondLand));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(legendary.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(legendary);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstLand, bear, secondLand);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE)))
                .hasSize(3);
    }

    @Test
    @DisplayName("-11 steals, untaps, and gives haste to all nonland permanents")
    void minusElevenStealsUntapsAndGivesHasteToAllNonlands() {
        Permanent dihada = addReadyDihada(player1, 11);
        Permanent sisay = harness.addToBattlefieldAndReturn(player1, new CaptainSisay());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        sisay.tap();
        bear.tap();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sisay, bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(bear.isTapped()).isFalse();
        assertThat(sisay.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
    }

    @Test
    void plusTwoCanTargetOpponentsLegendaryCreature() {
        addReadyDihada(player1, 5);
        Permanent sisay = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());

        harness.activateAbility(player1, 0, 0, null, sisay.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sisay, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void plusTwoCanResolveWithoutATarget() {
        Permanent dihada = addReadyDihada(player1, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusTwoKeywordsLastThroughOpponentsTurnAndExpireAtYourNextTurn() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        addReadyDihada(player1, 5);
        Permanent sisay = harness.addToBattlefieldAndReturn(player1, new CaptainSisay());

        harness.activateAbility(player1, 0, 0, null, sisay.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, sisay, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, sisay, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void minusThreeCanDeclineAllLegendaryCards() {
        addReadyDihada(player1, 5);
        CaptainSisay sisay = new CaptainSisay();
        DihadaBinderOfWills otherDihada = new DihadaBinderOfWills();
        Forest land = new Forest();
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(sisay, otherDihada, land, bear));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(sisay, otherDihada, land, bear);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(sisay, otherDihada);
        assertTreasureCount(4);
    }

    @Test
    void minusThreeCanKeepLegendaryNoncreaturesAndAllFourCards() {
        addReadyDihada(player1, 5);
        DihadaBinderOfWills first = new DihadaBinderOfWills();
        DihadaBinderOfWills second = new DihadaBinderOfWills();
        CaptainSisay third = new CaptainSisay();
        CaptainSisay fourth = new CaptainSisay();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId(), fourth.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second, third, fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertTreasureCount(0);
    }

    @Test
    void minusThreeWithShortLibraryCreatesOnlyTreasuresForCardsPutInGraveyard() {
        addReadyDihada(player1, 5);
        CaptainSisay sisay = new CaptainSisay();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(sisay, land));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sisay.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(sisay);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertTreasureCount(1);
    }

    @Test
    void minusThreeWithEmptyLibraryCreatesNoTreasures() {
        addReadyDihada(player1, 5);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertTreasureCount(0);
    }

    @Test
    void minusElevenControlAndHasteExpireAndLandsRemainUnaffected() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        addReadyDihada(player1, 12);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        ownLand.tap();
        opposingLand.tap();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingLand);
        assertThat(ownLand.isTapped()).isTrue();
        assertThat(opposingLand.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingLand, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
    }

    @Test
    @CardUsed({DihadaBinderOfWills.class, Forest.class, RestInPeace.class})
    void minusThreeCreatesNoTreasuresWhenRevealedCardsAreExiledInstead() {
        addReadyDihada(player1, 5);
        harness.addToBattlefield(player2, new RestInPeace());
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second, third, fourth);
        assertTreasureCount(0);
    }

    @Test
    void minusElevenAlsoStealsUntapsAndGrantsHasteToNoncreaturesAfterSourceDies() {
        Permanent source = addReadyDihada(player1, 11);
        Permanent opposingDihada = harness.addToBattlefieldAndReturn(player2, new DihadaBinderOfWills());
        opposingDihada.setCounterCount(CounterType.LOYALTY, 5);
        opposingDihada.tap();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source).contains(opposingDihada);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
        assertThat(opposingDihada.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingDihada, Keyword.HASTE)).isTrue();
    }

    private void assertTreasureCount(int expected) {
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE)))
                .hasSize(expected);
    }

    private Permanent addReadyDihada(Player player, int loyalty) {
        Permanent dihada = harness.addToBattlefieldAndReturn(player, new DihadaBinderOfWills());
        dihada.setCounterCount(CounterType.LOYALTY, loyalty);
        dihada.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return dihada;
    }
}
