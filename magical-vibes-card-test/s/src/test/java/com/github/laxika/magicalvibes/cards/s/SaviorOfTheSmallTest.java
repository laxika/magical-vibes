package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FearOfBeingHunted;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaviorOfTheSmall.class, GrizzlyBears.class, SibilantSpirit.class, Forest.class,
        FearOfBeingHunted.class, Murder.class, RelentlessAssault.class})
class SaviorOfTheSmallTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Savior returns a target creature with mana value 3 or less to hand")
    void tappedSaviorReturnsCheapCreatureToHand() {
        Permanent savior = addSavior();
        savior.tap();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        advanceToPostcombatMain(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only qualifying creatures from its controller's graveyard are legal targets")
    void onlyQualifyingCreaturesAreLegalTargets() {
        Permanent savior = addSavior();
        savior.tap();
        Card cheapCreature = new GrizzlyBears();
        Card expensiveCreature = new SibilantSpirit();
        Card noncreature = new Forest();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(cheapCreature, expensiveCreature, noncreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        advanceToPostcombatMain(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(cheapCreature.getId());
    }

    @Test
    @DisplayName("The survival ability does not return a card if Savior becomes untapped before resolution")
    void untappingBeforeResolutionPreventsReturn() {
        Permanent savior = addSavior();
        savior.tap();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        advanceToPostcombatMain(player1);
        savior.untap();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("The survival ability does not trigger while Savior is untapped")
    void untappedSaviorDoesNotTrigger() {
        addSavior();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("A creature with mana value exactly three can be returned")
    void returnsCreatureAtManaValueLimit() {
        addSavior().tap();
        Card creature = new FearOfBeingHunted();
        harness.setGraveyard(player1, List.of(creature));

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fear of Being Hunted");
        harness.assertNotInGraveyard(player1, "Fear of Being Hunted");
    }

    @Test
    @DisplayName("A tapped Savior does not trigger during the opponent's second main phase")
    void doesNotTriggerOnOpponentsTurn() {
        addSavior().tap();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A tapped Savior with no legal graveyard target creates no stack entry")
    void noLegalTargetCreatesNoStackEntry() {
        addSavior().tap();
        harness.setGraveyard(player1, List.of(new SaviorOfTheSmall(), new Forest()));

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Untapping and retapping Savior before resolution still returns the target")
    void retappedSaviorReturnsTarget() {
        Permanent savior = addSavior();
        savior.tap();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        savior.untap();
        savior.tap();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Savior destroyed while tapped still returns the target using its last known status")
    void destroyedTappedSaviorReturnsTarget() {
        Permanent savior = addSavior();
        savior.tap();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, savior.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Savior of the Small");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Savior untapped before it is destroyed does not return the target")
    void destroyedUntappedSaviorDoesNotReturnTarget() {
        Permanent savior = addSavior();
        savior.tap();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        savior.untap();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, savior.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Savior of the Small");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An ability whose target leaves the graveyard does not return another creature")
    void doesNotChooseAnotherTargetAtResolution() {
        addSavior().tap();
        Card target = new GrizzlyBears();
        Card otherCreature = new FearOfBeingHunted();
        harness.setGraveyard(player1, List.of(target, otherCreature));

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherCreature));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Fear of Being Hunted");
        harness.assertInGraveyard(player1, "Fear of Being Hunted");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Survival does not trigger again in a third main phase")
    void doesNotTriggerAgainInThirdMainPhase() {
        addSavior().tap();
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId()));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secondCreature);
    }

    private Permanent addSavior() {
        return harness.addToBattlefieldAndReturn(player1, new SaviorOfTheSmall());
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
