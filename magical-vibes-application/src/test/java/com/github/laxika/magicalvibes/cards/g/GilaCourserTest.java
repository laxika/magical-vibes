package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GilaCourser.class, GrizzlyBears.class})
class GilaCourserTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 1 taps another creature and enables the attack trigger")
    void saddleEnablesAttackTrigger() {
        Permanent courser = addCreatureReady(player1, new GilaCourser());
        Permanent helper = addCreatureReady(player1, new GilaCourser());
        Card topCard = new GilaCourser();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, helper.getId());
        resolveAllTriggers();

        assertThat(helper.isTapped()).isTrue();
        assertThat(courser.isSaddled()).isTrue();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("A saddled attack with an empty library exiles nothing")
    void emptyLibraryDoesNotExileAnything() {
        Permanent courser = addCreatureReady(player1, new GilaCourser());
        courser.setSaddled(true);
        harness.setLibrary(player1, List.of());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger resolves after its source leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent courser = addCreatureReady(player1, new GilaCourser());
        courser.setSaddled(true);
        Card topCard = new GilaCourser();
        Card secondCard = new GilaCourser();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(courser);
        gd.playerGraveyards.get(player1.getId()).add(courser.getCard());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The exiled creature can be cast by paying its normal mana cost")
    void castsExiledCreature() {
        Permanent courser = addCreatureReady(player1, new GilaCourser());
        courser.setSaddled(true);
        Card topCard = new GilaCourser();
        harness.setLibrary(player1, List.of(topCard));
        int attackTurn = gd.turnNumber;
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(topCard.getId())).isEqualTo(attackTurn + 2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Attacking while saddled exiles the top card with play permission")
    void attacksWhileSaddled() {
        Card topCard = new GrizzlyBears();
        Permanent courser = addCreatureReady(player1, new GilaCourser());
        courser.setSaddled(true);
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Attacking while unsaddled does not exile the top card")
    void doesNotTriggerWhileUnsaddled() {
        Card topCard = new GrizzlyBears();
        addCreatureReady(player1, new GilaCourser());
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Card topCard = new GrizzlyBears();
        Permanent courser = addCreatureReady(player1, new GilaCourser());
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        courser.setSaddled(true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
}
