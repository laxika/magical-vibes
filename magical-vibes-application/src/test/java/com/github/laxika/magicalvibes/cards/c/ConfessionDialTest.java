package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YoshimaruEverFaithful;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConfessionDial.class, YoshimaruEverFaithful.class, GrizzlyBears.class, Forest.class, Shock.class,
        PullFromEternity.class})
class ConfessionDialTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and surveils three cards")
    void entersAndSurveilsThree() {
        List<Card> library = List.of(new Forest(), new Shock(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ConfessionDial()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(library.get(0), library.get(1), library.get(2));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of(2)));

        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(library.get(0), library.get(1));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(library.get(2));
    }

    @Test
    void surveilCanKeepAllCardsInADifferentOrder() {
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ConfessionDial()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 0, 1), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(2), library.get(0), library.get(1), library.get(3));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilsAllAvailableCardsWhenLibraryHasFewerThanThree() {
        List<Card> library = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ConfessionDial()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactlyElementsOf(library);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    void surveilingAnEmptyLibraryDoesNotRequireAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ConfessionDial()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Confession Dial");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Grants escape to a targeted legendary creature card")
    void grantsEscapeToTargetedLegendaryCreature() {
        Permanent dial = addReadyDial();
        YoshimaruEverFaithful target = new YoshimaruEverFaithful();
        Forest firstExile = new Forest();
        Shock secondExile = new Shock();
        Forest thirdExile = new Forest();
        harness.setGraveyard(player1, List.of(target, firstExile, secondExile, thirdExile));
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareMainPhase();

        int dialIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dial);
        harness.activateAbility(player1, dialIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        Permanent escaped = findPermanent(player1, "Yoshimaru, Ever Faithful");
        assertThat(escaped.isEscaped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(
                firstExile, secondExile, thirdExile);
    }

    @Test
    void canTapDialOnTheTurnItEnters() {
        YoshimaruEverFaithful target = new YoshimaruEverFaithful();
        harness.setGraveyard(player1, List.of(target, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ConfessionDial()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareMainPhase();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
        int dialIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Confession Dial"));
        harness.activateAbility(player1, dialIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Yoshimaru, Ever Faithful").isEscaped()).isTrue();
    }

    @Test
    void grantingEscapeDoesNotChangeCreatureSpellTiming() {
        Permanent dial = addReadyDial();
        YoshimaruEverFaithful target = new YoshimaruEverFaithful();
        harness.setGraveyard(player1, List.of(target, new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int dialIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dial);
        harness.activateAbility(player1, dialIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4).contains(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only legendary creature cards can be targeted")
    void onlyLegendaryCreatureCardsAreTargetable() {
        Permanent dial = addReadyDial();
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        prepareMainPhase();

        int dialIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dial);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, dialIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsLegendaryCreature() {
        Permanent dial = addReadyDial();
        YoshimaruEverFaithful target = new YoshimaruEverFaithful();
        harness.setGraveyard(player2, List.of(target));
        prepareMainPhase();

        int dialIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dial);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, dialIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dial.isTapped()).isFalse();
    }

    @Test
    void escapeRequiresThreeDistinctOtherCards() {
        Permanent dial = addReadyDial();
        YoshimaruEverFaithful target = new YoshimaruEverFaithful();
        harness.setGraveyard(player1, List.of(target, new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareMainPhase();
        int dialIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dial);
        harness.activateAbility(player1, dialIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(dial.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4).contains(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Yoshimaru, Ever Faithful");
    }

    @Test
    void canGrantEscapeWithoutEnoughCardsToPayIt() {
        Permanent dial = addReadyDial();
        YoshimaruEverFaithful target = new YoshimaruEverFaithful();
        harness.setGraveyard(player1, List.of(target, new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareMainPhase();
        int dialIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dial);
        harness.activateAbility(player1, dialIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(dial.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void escapeRequiresTheCardsColoredManaCost() {
        Permanent dial = addReadyDial();
        YoshimaruEverFaithful target = new YoshimaruEverFaithful();
        harness.setGraveyard(player1, List.of(target, new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();
        int dialIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dial);
        harness.activateAbility(player1, dialIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4).contains(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void grantedEscapeSurvivesDialLeavingTheBattlefield() {
        Permanent dial = addReadyDial();
        YoshimaruEverFaithful target = new YoshimaruEverFaithful();
        harness.setGraveyard(player1, List.of(target, new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareMainPhase();
        int dialIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dial);
        harness.activateAbility(player1, dialIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(dial);
        gd.addCardToHand(player1.getId(), dial.getCard());

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Yoshimaru, Ever Faithful").isEscaped()).isTrue();
    }

    @Test
    void grantedEscapeExpiresAtEndOfTurn() {
        harness.setHand(player2, List.of());
        Permanent dial = addReadyDial();
        YoshimaruEverFaithful target = new YoshimaruEverFaithful();
        harness.setGraveyard(player1, List.of(target, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        prepareMainPhase();
        int dialIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dial);
        harness.activateAbility(player1, dialIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
    }

    @Test
    void escapeDoesNotPersistWhenTheCardLeavesAndReturnsToGraveyard() {
        Permanent firstDial = addReadyDial();
        Permanent secondDial = addReadyDial();
        YoshimaruEverFaithful target = new YoshimaruEverFaithful();
        YoshimaruEverFaithful otherCreature = new YoshimaruEverFaithful();
        harness.setGraveyard(player1, List.of(target, otherCreature,
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        prepareMainPhase();

        int firstIndex = gd.playerBattlefields.get(player1.getId()).indexOf(firstDial);
        int secondIndex = gd.playerBattlefields.get(player1.getId()).indexOf(secondDial);
        harness.activateAbility(player1, firstIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.activateAbility(player1, secondIndex, 0, null, otherCreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.castFromGraveyard(player1, 1, List.of(0, 1, 2));
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        int targetIndex = gd.playerGraveyards.get(player1.getId()).indexOf(target);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, targetIndex, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDial() {
        Permanent dial = harness.addToBattlefieldAndReturn(player1, new ConfessionDial());
        dial.setSummoningSick(false);
        return dial;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
