package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;




@CardUsed({MerenOfClanNelToth.class, GrizzlyBears.class, HillGiant.class, AirElemental.class, Shock.class})
class MerenOfClanNelTothTest extends BaseCardTest {

    @Test
    void gainsExperienceWhenAnotherCreatureYouControlDies() {
        addMeren();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void returnsTargetToBattlefieldWhenManaValueIsWithinExperience() {
        addMeren();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        gd.playerExperienceCounters.put(player1.getId(), 2);

        advanceToEndStep(player1);
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnsTargetToHandWhenManaValueExceedsExperience() {
        addMeren();
        Card target = new HillGiant();
        harness.setGraveyard(player1, List.of(target));
        gd.playerExperienceCounters.put(player1.getId(), 2);

        advanceToEndStep(player1);
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hill Giant");
        harness.assertNotInGraveyard(player1, "Hill Giant");
    }

    private Permanent addMeren() {
        return harness.addToBattlefieldAndReturn(player1, new MerenOfClanNelToth());
    }

    private void chooseTarget(Card target) {
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
    @Test
    void returnsCreatureToBattlefieldWhenManaValueIsWithinExperience() {
        Card target = new GrizzlyBears();
        harness.addToBattlefield(player1, new MerenOfClanNelToth());
        harness.setGraveyard(player1, List.of(target));
        gd.playerExperienceCounters.put(player1.getId(), 2);

        advanceToEndStep();
        chooseGraveyardTarget(target);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnsCreatureToHandWhenManaValueExceedsExperience() {
        Card target = new AirElemental();
        harness.addToBattlefield(player1, new MerenOfClanNelToth());
        harness.setGraveyard(player1, List.of(target));
        gd.playerExperienceCounters.put(player1.getId(), 2);

        advanceToEndStep();
        chooseGraveyardTarget(target);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Air Elemental");
        harness.assertNotInGraveyard(player1, "Air Elemental");
    }

    @Test
    void endStepAbilityOnlyTargetsCreatureCardsInYourGraveyard() {
        Card target = new GrizzlyBears();
        harness.addToBattlefield(player1, new MerenOfClanNelToth());
        harness.setGraveyard(player1, List.of(new Shock(), target));
        gd.playerExperienceCounters.put(player1.getId(), 2);

        advanceToEndStep();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
    }

    private void advanceToEndStep() {
        advanceToEndStep(player1);
    }

    private void chooseGraveyardTarget(Card target) {
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
    }

    @Test
    void doesNotGainExperienceForItsOwnDeath() {
        Permanent meren = addMeren();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, meren));
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void doesNotGainExperienceForOpponentsCreatureDeath() {
        addMeren();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void usesExperienceCountersAtResolution() {
        addMeren();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        gd.playerExperienceCounters.put(player1.getId(), 1);
        advanceToEndStep();
        chooseTarget(target);

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();
        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
    }

    @Test
    void endStepAbilityResolvesAfterMerenLeavesBattlefield() {
        Permanent meren = addMeren();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        gd.playerExperienceCounters.put(player1.getId(), 2);
        advanceToEndStep();
        chooseTarget(target);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, meren));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        addMeren();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        gd.playerExperienceCounters.put(player1.getId(), 2);
        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnsCreatureToHandWithNoExperienceCounters() {
        addMeren();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        advanceToEndStep();
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doesNotReturnTargetThatLeftGraveyardBeforeResolution() {
        addMeren();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        gd.playerExperienceCounters.put(player1.getId(), 2);
        advanceToEndStep();
        chooseTarget(target);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target);
    }

}
