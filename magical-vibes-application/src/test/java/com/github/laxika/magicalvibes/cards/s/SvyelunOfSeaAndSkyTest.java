package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.m.Murder;
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

@CardUsed({SvyelunOfSeaAndSky.class, MerfolkOfThePearlTrident.class, Murder.class,
        GrizzlyBears.class})
class SvyelunOfSeaAndSkyTest extends BaseCardTest {

    @Test
    @DisplayName("Has indestructible while controlling two other Merfolk")
    void hasIndestructibleWithTwoOtherMerfolk() {
        Permanent svyelun = addCreatureReady(player1, new SvyelunOfSeaAndSky());
        addCreatureReady(player1, new MerfolkOfThePearlTrident());
        addCreatureReady(player1, new MerfolkOfThePearlTrident());

        assertThat(gqs.hasKeyword(gd, svyelun, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Loses indestructible below two other Merfolk")
    void losesIndestructibleBelowThreshold() {
        Permanent svyelun = addCreatureReady(player1, new SvyelunOfSeaAndSky());
        addCreatureReady(player1, new MerfolkOfThePearlTrident());

        assertThat(gqs.hasKeyword(gd, svyelun, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Draws a card when it attacks")
    void attackingDrawsCard() {
        addCreatureReady(player1, new SvyelunOfSeaAndSky());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MerfolkOfThePearlTrident()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Gives other Merfolk ward 1")
    void givesOtherMerfolkWardOne() {
        harness.addToBattlefield(player1, new SvyelunOfSeaAndSky());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkOfThePearlTrident());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player2, 0, merfolk.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Merfolk of the Pearl Trident");
        harness.assertInGraveyard(player2, "Murder");
    }

    @Test
    @DisplayName("Does not give ward to non-Merfolk creatures")
    void doesNotGiveWardToNonMerfolk() {
        harness.addToBattlefield(player1, new SvyelunOfSeaAndSky());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Murder");
    }
}
