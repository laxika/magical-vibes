package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmsRace.class, GrizzlyBears.class, Ornithopter.class})
class ArmsRaceTest extends BaseCardTest {

    @Test
    @DisplayName("Ability offers only artifact cards in hand")
    void abilityOffersOnlyArtifacts() {
        addReadyArmsRace();
        harness.setHand(player1, List.of(new GrizzlyBears(), new Ornithopter()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Chosen artifact enters with haste and is scheduled for end-step sacrifice")
    void chosenArtifactEntersWithHasteAndEndStepSacrifice() {
        addReadyArmsRace();
        harness.setHand(player1, List.of(new Ornithopter()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        assertThat(ornithopter.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(ornithopter.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("Declining the may leaves the artifact in hand")
    void decliningLeavesArtifactInHand() {
        addReadyArmsRace();
        harness.setHand(player1, List.of(new Ornithopter()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("End-step sacrifice uses the stack and can be responded to")
    void sacrificeWaitsForItsTriggerToResolve() {
        putOrnithopterWithArmsRace();

        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Activating during the end step grants haste beyond cleanup")
    void hastePersistsAcrossCleanup() {
        harness.passUntil(TurnStep.END_STEP);
        putOrnithopterWithArmsRace();
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        assertThat(ornithopter.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("An artifact controlled by another player is not sacrificed")
    void artifactSurvivesAfterChangingController() {
        putOrnithopterWithArmsRace();
        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        gd.playerBattlefields.get(player1.getId()).remove(ornithopter);
        gd.playerBattlefields.get(player2.getId()).add(ornithopter);

        harness.passUntil(TurnStep.END_STEP);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Accepting with no artifact in hand finishes without a card choice")
    void noArtifactInHandFinishesResolution() {
        addReadyArmsRace();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void putOrnithopterWithArmsRace() {
        addReadyArmsRace();
        harness.setHand(player1, List.of(new Ornithopter()));
        addAbilityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }

    private void addReadyArmsRace() {
        harness.addToBattlefield(player1, new ArmsRace());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
