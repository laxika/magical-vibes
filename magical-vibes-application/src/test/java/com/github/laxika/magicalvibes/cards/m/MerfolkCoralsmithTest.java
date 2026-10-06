package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkCoralsmith.class})
class MerfolkCoralsmithTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives it +1/-1 until end of turn")
    void activationBoostsSelf() {
        Permanent coralsmith = addReadyCoralsmith();
        int basePower = gqs.getEffectivePower(gd, coralsmith);
        int baseToughness = gqs.getEffectiveToughness(gd, coralsmith);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, coralsmith)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, coralsmith)).isEqualTo(baseToughness - 1);
    }

    @Test
    @DisplayName("The temporary boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent coralsmith = addReadyCoralsmith();
        int basePower = gqs.getEffectivePower(gd, coralsmith);
        int baseToughness = gqs.getEffectiveToughness(gd, coralsmith);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, coralsmith)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, coralsmith)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("When it dies, it offers scry 2")
    void diesWithScryTwo() {
        Permanent coralsmith = addReadyCoralsmith();
        harness.setLibrary(player1, List.of(new MerfolkCoralsmith(), new MerfolkCoralsmith(), new MerfolkCoralsmith()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, coralsmith));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent coralsmith = harness.addToBattlefieldAndReturn(player1, new MerfolkCoralsmith());
        coralsmith.setSummoningSick(true);
        coralsmith.tap();
        int basePower = gqs.getEffectivePower(gd, coralsmith);
        int baseToughness = gqs.getEffectiveToughness(gd, coralsmith);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, coralsmith)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, coralsmith)).isEqualTo(baseToughness - 1);
        assertThat(coralsmith.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Repeated activations stack and zero toughness causes death and scry")
    void repeatedActivationsCauseDeathAndScry() {
        Permanent coralsmith = addReadyCoralsmith();
        int basePower = gqs.getEffectivePower(gd, coralsmith);
        int baseToughness = gqs.getEffectiveToughness(gd, coralsmith);
        MerfolkCoralsmith first = new MerfolkCoralsmith();
        MerfolkCoralsmith second = new MerfolkCoralsmith();
        MerfolkCoralsmith third = new MerfolkCoralsmith();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int activation = 1; activation <= 2; activation++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            assertThat(gqs.getEffectivePower(gd, coralsmith)).isEqualTo(basePower + activation);
            assertThat(gqs.getEffectiveToughness(gd, coralsmith)).isEqualTo(baseToughness - activation);
        }
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Merfolk Coralsmith");
        harness.assertInGraveyard(player1, "Merfolk Coralsmith");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The death trigger scries the dying creature's controller's library")
    void opponentDeathScriesOpponentLibrary() {
        Permanent coralsmith = harness.addToBattlefieldAndReturn(player2, new MerfolkCoralsmith());
        MerfolkCoralsmith first = new MerfolkCoralsmith();
        MerfolkCoralsmith second = new MerfolkCoralsmith();
        MerfolkCoralsmith third = new MerfolkCoralsmith();
        MerfolkCoralsmith untouched = new MerfolkCoralsmith();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setLibrary(player1, List.of(untouched));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, coralsmith));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third, second, first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 with one card in the library only offers that card")
    void deathWithOneCardLibrary() {
        Permanent coralsmith = addReadyCoralsmith();
        MerfolkCoralsmith onlyCard = new MerfolkCoralsmith();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, coralsmith));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry with an empty library resolves without a choice or a draw")
    void deathWithEmptyLibrary() {
        Permanent coralsmith = addReadyCoralsmith();
        harness.setLibrary(player1, List.of());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, coralsmith));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Merfolk Coralsmith");
    }

    private Permanent addReadyCoralsmith() {
        return addCreatureReady(player1, new MerfolkCoralsmith());
    }
}
