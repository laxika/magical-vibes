package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.f.FlickerOfFate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DireFleetWarmonger.class, AlpineWatchdog.class})
class DireFleetWarmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives Dire Fleet Warmonger +2/+2 and trample")
    void sacrificingAnotherCreatureBoostsAndGrantsTrample() {
        Permanent warmonger = addCreatureReady(player1, new DireFleetWarmonger());
        Permanent watchdog = addCreatureReady(player1, new AlpineWatchdog());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, watchdog.getId());

        assertThat(gqs.getEffectivePower(gd, warmonger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, warmonger)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, warmonger, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(watchdog.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        Permanent warmonger = addCreatureReady(player1, new DireFleetWarmonger());
        Permanent watchdog = addCreatureReady(player1, new AlpineWatchdog());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, warmonger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warmonger)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warmonger, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(watchdog);
    }

    @Test
    @DisplayName("The ability does nothing when there is no other creature")
    void noOtherCreatureDoesNothing() {
        Permanent warmonger = addCreatureReady(player1, new DireFleetWarmonger());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, warmonger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warmonger)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warmonger, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability triggers only during its controller's combat")
    void doesNotTriggerDuringOpponentCombat() {
        addCreatureReady(player1, new DireFleetWarmonger());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void boostAndTrampleWearOffAtEndOfTurn() {
        Permanent warmonger = addCreatureReady(player1, new DireFleetWarmonger());
        Permanent watchdog = addCreatureReady(player1, new AlpineWatchdog());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, watchdog.getId());

        assertThat(gqs.hasKeyword(gd, warmonger, Keyword.TRAMPLE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warmonger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warmonger)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warmonger, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Only another creature you control can be sacrificed")
    void sacrificeChoiceExcludesSourceAndOpponentCreature() {
        Permanent warmonger = addCreatureReady(player1, new DireFleetWarmonger());
        Permanent ownCreature = addCreatureReady(player1, new AlpineWatchdog());
        Permanent opponentCreature = addCreatureReady(player2, new AlpineWatchdog());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(ownCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(warmonger);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gqs.getEffectivePower(gd, warmonger)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, warmonger, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("One trigger sacrifices only one creature even when several are available")
    void sacrificesOnlyOneCreature() {
        Permanent warmonger = addCreatureReady(player1, new DireFleetWarmonger());
        Permanent sacrificed = addCreatureReady(player1, new AlpineWatchdog());
        Permanent remaining = addCreatureReady(player1, new AlpineWatchdog());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sacrificed.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(warmonger, remaining);
        assertThat(gqs.getEffectivePower(gd, warmonger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, warmonger)).isEqualTo(5);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @CardUsed({FlickerOfFate.class})
    @DisplayName("An old trigger does not boost Warmonger after it leaves and returns")
    void oldTriggerDoesNotBoostReturnedWarmonger() {
        Permanent original = addCreatureReady(player1, new DireFleetWarmonger());
        Permanent sacrifice = addCreatureReady(player1, new AlpineWatchdog());
        harness.setHand(player1, List.of(new FlickerOfFate()));
        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, original.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Dire Fleet Warmonger");
        assertThat(returned.getId()).isNotEqualTo(original.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @CardUsed({FlickerOfFate.class})
    @DisplayName("The returned Warmonger is another creature for its old trigger")
    void oldTriggerCanSacrificeReturnedWarmonger() {
        Permanent original = addCreatureReady(player1, new DireFleetWarmonger());
        harness.setHand(player1, List.of(new FlickerOfFate()));
        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, original.getId());
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Dire Fleet Warmonger");
        assertThat(returned.getId()).isNotEqualTo(original.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(returned.getId());
        harness.handlePermanentChosen(player1, returned.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned.getCard());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
