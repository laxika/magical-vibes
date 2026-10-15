package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmugglersSurprise.class, Forest.class, GrizzlyBears.class, SerraAngel.class})
class SmugglersSurpriseTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode mills four and can return up to two creature or land cards")
    void millsAndReturnsUpToTwoMatchingCards() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears, new GrizzlyBears(), new GrizzlyBears()));
        cast(new SmugglersSurprise(), 0, 3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The second mode puts up to two creature cards from hand onto the battlefield")
    void putsUpToTwoCreaturesFromHandOntoBattlefield() {
        SmugglersSurprise spell = new SmugglersSurprise();
        SerraAngel angel = new SerraAngel();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(spell, angel, bears));
        cast(spell, 1, 6);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(angel.getId(), bears.getId()));

        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The third mode protects qualifying creatures and expires at end of turn")
    void protectsCreaturesWithPowerAtLeastFourUntilEndOfTurn() {
        Permanent angel = addCreatureReady(player1, new SerraAngel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        cast(new SmugglersSurprise(), 2, 2);

        assertThat(gqs.hasKeyword(gd, angel, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, angel, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("All three modes resolve in printed order, including returning and deploying milled creatures")
    void allModesReturnDeployAndProtectCreatures() {
        SerraAngel angel = new SerraAngel();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        SmugglersSurprise milledInstant = new SmugglersSurprise();
        harness.setLibrary(player1, List.of(angel, bears, forest, milledInstant));
        castModes(new SmugglersSurprise(), 9, 2, 1, 0);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, milledInstant);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(angel.getId(), bears.getId()));

        Permanent deployedAngel = findPermanent(player1, "Serra Angel");
        Permanent deployedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, deployedAngel, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, deployedAngel, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, deployedBears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, deployedBears, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The first mode can decline every return and mills only the remaining library")
    void mayReturnNoCardsFromShortLibrary() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        SmugglersSurprise instant = new SmugglersSurprise();
        harness.setLibrary(player1, List.of(forest, bears, instant));
        cast(new SmugglersSurprise(), 0, 3);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, bears, instant);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The second mode can leave all eligible creatures in hand")
    void mayPutNoCreaturesOntoBattlefield() {
        SmugglersSurprise spell = new SmugglersSurprise();
        SerraAngel angel = new SerraAngel();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(spell, angel, forest));
        cast(spell, 1, 6);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(angel, forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The first mode can choose a later milled card without returning an older graveyard card")
    void mayReturnOnlyOneLaterMilledCard() {
        Forest forest = new Forest();
        SerraAngel angel = new SerraAngel();
        GrizzlyBears olderCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(olderCard));
        harness.setLibrary(player1, List.of(forest, angel, new SmugglersSurprise(), new SmugglersSurprise()));
        cast(new SmugglersSurprise(), 0, 3);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(angel);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, olderCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The second mode cannot put land cards onto the battlefield")
    void cannotChooseLandForSecondMode() {
        SmugglersSurprise spell = new SmugglersSurprise();
        SerraAngel angel = new SerraAngel();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(spell, angel, forest));
        cast(spell, 1, 6);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(angel.getId()));

        harness.assertOnBattlefield(player1, "Serra Angel");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(findPermanent(player1, "Serra Angel").isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Protection uses current power at resolution and remains after power drops")
    void protectionIsFixedAtResolution() {
        Permanent boostedBears = addCreatureReady(player1, new GrizzlyBears());
        boostedBears.setPowerModifier(2);
        Permanent weakenedAngel = addCreatureReady(player1, new SerraAngel());
        weakenedAngel.setPowerModifier(-1);
        Permanent opposingAngel = addCreatureReady(player2, new SerraAngel());
        cast(new SmugglersSurprise(), 2, 2);

        boostedBears.setPowerModifier(0);
        weakenedAngel.setPowerModifier(0);
        Permanent laterAngel = addCreatureReady(player1, new SerraAngel());

        assertThat(gqs.hasKeyword(gd, boostedBears, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, boostedBears, Keyword.INDESTRUCTIBLE)).isTrue();
        for (Permanent unprotected : List.of(weakenedAngel, opposingAngel, laterAngel)) {
            assertThat(gqs.hasKeyword(gd, unprotected, Keyword.HEXPROOF)).isFalse();
            assertThat(gqs.hasKeyword(gd, unprotected, Keyword.INDESTRUCTIBLE)).isFalse();
        }
    }

    @Test
    @DisplayName("Casting multiple modes requires paying every selected additional cost")
    void cannotCastAllModesWithInsufficientMana() {
        harness.setHand(player1, List.of(new SmugglersSurprise()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 3, new int[]{0, 1, 2}, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Smuggler's Surprise");
        assertThat(gd.stack).isEmpty();
    }

    private void cast(SmugglersSurprise spell, int mode, int totalMana) {
        castModes(spell, totalMana, mode);
    }

    private void castModes(SmugglersSurprise spell, int totalMana, int... modes) {
        if (gd.playerHands.get(player1.getId()).stream().noneMatch(card -> card == spell)) {
            harness.setHand(player1, List.of(spell));
        }
        harness.addMana(player1, ManaColor.GREEN, totalMana);
        harness.castModalInstantWithModes(player1, 0, 1, 3, modes, List.of());
        harness.passBothPriorities();
    }
}
