package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AerithLastAncient.class, GrizzlyBears.class, Shock.class})
class AerithLastAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the targeted creature to hand after gaining life")
    void returnsTargetToHandBelowSevenLife() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(target));
        gd.lifeGainedThisTurn.put(player1.getId(), 6);

        advanceToEndStep(player1);
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns the targeted creature to the battlefield after gaining 7 life")
    void returnsTargetToBattlefieldAtSevenLife() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(target));
        gd.lifeGainedThisTurn.put(player1.getId(), 7);

        advanceToEndStep(player1);
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when no life was gained")
    void doesNotTriggerWithoutLifeGain() {
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not target noncreature cards")
    void doesNotTargetNoncreatureCards() {
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(new Shock()));
        gd.lifeGainedThisTurn.put(player1.getId(), 7);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Shock");
    }

    private void chooseTarget(GrizzlyBears target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
    }

    @Test
    @DisplayName("Only triggers during its controller's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 7);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent life gain does not enable Raise")
    void doesNotTriggerFromOpponentsLifeGain() {
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player2.getId(), 7);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot return a creature from the opponent's graveyard")
    void doesNotTargetOpponentsGraveyard() {
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 7);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Checks the seven-life threshold when the ability resolves")
    void additionalLifeGainBeforeResolutionReturnsToBattlefield() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(target));
        gd.lifeGainedThisTurn.put(player1.getId(), 6);

        advanceToEndStep(player1);
        chooseTarget(target);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Life loss does not subtract from the amount gained this turn")
    void returnsToBattlefieldDespiteSubsequentLifeLoss() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(target));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 7));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        advanceToEndStep(player1);
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A target removed from the graveyard is not returned")
    void doesNotReturnTargetExiledBeforeResolution() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(target));
        gd.lifeGainedThisTurn.put(player1.getId(), 7);

        advanceToEndStep(player1);
        chooseTarget(target);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anySatisfy(entry -> assertThat(entry.card()).isSameAs(target));
    }

    @Test
    @DisplayName("Aerith's lifelink enables Raise after combat")
    void combatLifelinkEnablesReturnToHand() {
        GrizzlyBears target = new GrizzlyBears();
        addCreatureReady(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player1, 23);
        advanceToEndStep(player1);
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Raise resolves even if Aerith leaves the battlefield")
    void returnsTargetAfterAerithLeavesBattlefield() {
        GrizzlyBears target = new GrizzlyBears();
        AerithLastAncient aerith = new AerithLastAncient();
        harness.addToBattlefield(player1, aerith);
        harness.setGraveyard(player1, List.of(target));
        gd.lifeGainedThisTurn.put(player1.getId(), 8);

        advanceToEndStep(player1);
        chooseTarget(target);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(target, aerith));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Aerith, Last Ancient");
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
