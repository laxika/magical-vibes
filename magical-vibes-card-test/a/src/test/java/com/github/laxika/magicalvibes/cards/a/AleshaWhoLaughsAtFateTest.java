package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AleshaWhoLaughsAtFate.class, CentaurCourser.class, HillGiant.class, Murder.class})
class AleshaWhoLaughsAtFateTest extends BaseCardTest {

    @Test
    void attackingPutsCounterAndRaidReturnsCreatureWithinPower() {
        Permanent alesha = addReadyAlesha();
        Card valid = new CentaurCourser();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(valid, tooExpensive));

        declareAttack();
        harness.passBothPriorities();
        assertThat(alesha.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(valid.getId());

        harness.handleMultipleCardsChosen(player1, List.of(valid.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Centaur Courser");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void raidDoesNotTriggerIfYouDidNotAttack() {
        addReadyAlesha();
        Card valid = new CentaurCourser();
        harness.setGraveyard(player1, List.of(valid));

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Centaur Courser");
    }

    @Test
    void targetMustStillFitAleshasPowerOnResolution() {
        Permanent alesha = addReadyAlesha();
        Card valid = new CentaurCourser();
        harness.setGraveyard(player1, List.of(valid));

        declareAttack();
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(valid.getId()));

        alesha.setPowerModifier(-3);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Centaur Courser");
        harness.assertNotOnBattlefield(player1, "Centaur Courser");
    }

    @Test
    void raidTriggersWhenAnotherCreatureAttacks() {
        Permanent alesha = addReadyAlesha();
        addCreatureReady(player1, new CentaurCourser());
        Card valid = new CentaurCourser();
        alesha.setPowerModifier(1);
        harness.setGraveyard(player1, List.of(valid));

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        assertThat(alesha.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(valid.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(valid);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(valid.getId()));
    }

    @Test
    void raidDoesNotTriggerOnOpponentsEndStep() {
        addReadyAlesha();
        Card valid = new CentaurCourser();
        harness.setGraveyard(player1, List.of(valid));
        declareAttack();
        harness.passBothPriorities();

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Centaur Courser");
        harness.assertNotOnBattlefield(player1, "Centaur Courser");
    }

    @Test
    void raidUsesLastKnownPowerWhenAleshaDiesBeforeResolution() {
        Permanent alesha = addReadyAlesha();
        Card valid = new CentaurCourser();
        harness.setGraveyard(player1, List.of(valid));
        declareAttack();
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(valid.getId()));

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, alesha.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alesha, Who Laughs at Fate");
        harness.assertOnBattlefield(player1, "Centaur Courser");
        harness.assertNotInGraveyard(player1, "Centaur Courser");
    }

    @Test
    void raidTargetsOnlyCreatureCardsInYourGraveyard() {
        addReadyAlesha();
        Card valid = new CentaurCourser();
        Card noncreature = new Murder();
        Card opponentsCreature = new CentaurCourser();
        harness.setGraveyard(player1, List.of(valid, noncreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));

        declareAttack();
        harness.passBothPriorities();
        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(valid.getId());
        harness.handleMultipleCardsChosen(player1, List.of(valid.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Centaur Courser");
        harness.assertInGraveyard(player1, "Murder");
        harness.assertInGraveyard(player2, "Centaur Courser");
    }

    private Permanent addReadyAlesha() {
        return addCreatureReady(player1, new AleshaWhoLaughsAtFate());
    }

    private void declareAttack() {
        declareAttackers(List.of(0));
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
