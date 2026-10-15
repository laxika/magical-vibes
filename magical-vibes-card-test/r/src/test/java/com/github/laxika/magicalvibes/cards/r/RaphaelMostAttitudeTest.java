package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BuzzBots;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaphaelMostAttitude.class, Divination.class, Forest.class, GrizzlyBears.class, BuzzBots.class})
class RaphaelMostAttitudeTest extends BaseCardTest {

    @Test
    void allianceMayExileTopCardOfYourLibraryWithRaphael() {
        Permanent raphael = addRaphael();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getCardsExiledByPermanent(raphael.getId())).containsExactly(topCard);
    }

    @Test
    void decliningAllianceLeavesTopCardInYourLibrary() {
        addRaphael();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void attackingAllowsPlayingOnePreviouslyExiledCardUntilEndOfTurn() {
        Permanent raphael = addRaphael();
        Card spell = new Divination();
        Card land = new Forest();
        gd.addToExile(player1.getId(), spell, raphael.getId());
        gd.addToExile(player1.getId(), land, raphael.getId());

        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .isEmpty();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .containsExactlyInAnyOrder(spell.getId(), land.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(raphael.getId())).containsExactly(land);
        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .isEmpty();
    }

    @Test
    void attackPlayPermissionExpiresAtEndOfTurn() {
        Permanent raphael = addRaphael();
        Card exiled = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiled, raphael.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .contains(exiled.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .doesNotContain(exiled.getId());
        assertThat(gd.getCardsExiledByPermanent(raphael.getId())).containsExactly(exiled);
    }

    @Test
    void attackPermissionIncludesCardsExiledLaterInTheTurn() {
        Permanent raphael = addRaphael();
        Card earlierCard = new Forest();
        gd.addToExile(player1.getId(), earlierCard, raphael.getId());
        assertCanPlayCardExiledAfterAttacking(raphael);
    }

    @Test
    void attackPermissionExistsEvenWhenNoCardsWereExiledAtResolution() {
        assertCanPlayCardExiledAfterAttacking(addRaphael());
    }

    private void assertCanPlayCardExiledAfterAttacking(Permanent raphael) {
        Card laterCard = new Forest();
        harness.setLibrary(player1, List.of(laterCard));
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new BuzzBots(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getCardsExiledByPermanent(raphael.getId())).contains(laterCard);
        harness.castFromExile(player1, laterCard.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(laterCard.getId()));
        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .isEmpty();
    }

    @Test
    void playingAnExiledLandConsumesTheOneCardPermission() {
        Permanent raphael = addRaphael();
        Card land = new Forest();
        Card creature = new BuzzBots();
        gd.addToExile(player1.getId(), land, raphael.getId());
        gd.addToExile(player1.getId(), creature, raphael.getId());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, land.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(land.getId()));
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.getCardsExiledByPermanent(raphael.getId())).containsExactly(creature);
        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .isEmpty();
    }

    @Test
    void attackPermissionDoesNotBypassCreatureTimingOrManaCosts() {
        Permanent raphael = addRaphael();
        Card creature = new BuzzBots();
        gd.addToExile(player1.getId(), creature, raphael.getId());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getCardsExiledByPermanent(raphael.getId())).containsExactly(creature);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void attackPermissionRespectsLandTimingAndTheLandPlayLimit() {
        Permanent raphael = addRaphael();
        Card exiledLand = new Forest();
        gd.addToExile(player1.getId(), exiledLand, raphael.getId());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player1, exiledLand.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        assertThatThrownBy(() -> harness.castFromExile(player1, exiledLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getCardsExiledByPermanent(raphael.getId())).containsExactly(exiledLand);
        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .contains(exiledLand.getId());
    }

    @Test
    void acceptingAllianceWithAnEmptyLibraryDoesNotLoseTheGame() {
        Permanent raphael = addRaphael();
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new BuzzBots(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getCardsExiledByPermanent(raphael.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void allianceDoesNotTriggerForRaphaelHimself() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new RaphaelMostAttitude(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void allianceDoesNotTriggerForAnOpponentsCreature() {
        Permanent raphael = addRaphael();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new BuzzBots(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getCardsExiledByPermanent(raphael.getId())).isEmpty();
    }

    private Permanent addRaphael() {
        Permanent raphael = harness.addToBattlefieldAndReturn(player1, new RaphaelMostAttitude());
        raphael.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return raphael;
    }
}
