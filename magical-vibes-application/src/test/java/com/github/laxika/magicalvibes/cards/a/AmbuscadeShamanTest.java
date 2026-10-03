package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UltimatePrice;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbuscadeShaman.class, GrizzlyBears.class, FugitiveWizard.class, UltimatePrice.class})
class AmbuscadeShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry gives it +2/+2 until end of turn")
    void ownEntryBoostsItself() {
        harness.setHand(player1, List.of(new AmbuscadeShaman()));
        addManaForNormalCast();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent shaman = findPermanent(player1, "Ambuscade Shaman");
        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(4);
    }

    @Test
    @DisplayName("Another creature entering under its control gets +2/+2 until end of turn")
    void anotherAllyEntryBoostsThatCreature() {
        harness.addToBattlefield(player1, new AmbuscadeShaman());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("It does not trigger for an opponent's creature")
    void opponentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new AmbuscadeShaman());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent wizard = findPermanent(player2, "Fugitive Wizard");
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AmbuscadeShaman());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dash grants haste and returns it to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new AmbuscadeShaman()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        Permanent shaman = findPermanent(player1, "Ambuscade Shaman");
        assertThat(shaman.hasKeyword(Keyword.HASTE)).isTrue();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Ambuscade Shaman");
        harness.assertNotOnBattlefield(player1, "Ambuscade Shaman");
    }

    @Test
    @DisplayName("Normal casting grants no haste and does not return the Shaman at end step")
    void normalCastStaysOnBattlefieldAtEndStep() {
        harness.setHand(player1, List.of(new AmbuscadeShaman()));
        addManaForNormalCast();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent shaman = findPermanent(player1, "Ambuscade Shaman");
        assertThat(gqs.hasKeyword(gd, shaman, Keyword.HASTE)).isFalse();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ambuscade Shaman");
        harness.assertNotInHand(player1, "Ambuscade Shaman");
    }

    @Test
    @DisplayName("Each Shaman boosts an entering Shaman, without boosting the existing one")
    void multipleShamansStackBoostsOnEnteringCreature() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new AmbuscadeShaman());
        harness.setHand(player1, List.of(new AmbuscadeShaman()));
        addManaForNormalCast();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(existing.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dash schedules its return during spell resolution, without an additional entry trigger")
    void dashOnlyCreatesThePrintedEntryTrigger() {
        harness.setHand(player1, List.of(new AmbuscadeShaman()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ambuscade Shaman");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Dash returns the creature only when its end-step trigger resolves")
    void dashReturnAllowsResponsesAtEndStep() {
        harness.setHand(player1, List.of(new AmbuscadeShaman()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Ambuscade Shaman");
        harness.assertNotInHand(player1, "Ambuscade Shaman");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertInHand(player1, "Ambuscade Shaman");
        harness.assertNotOnBattlefield(player1, "Ambuscade Shaman");
    }

    @Test
    @DisplayName("An entry boost still resolves after its source is destroyed")
    void boostSurvivesItsSourceLeaving() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new AmbuscadeShaman());
        harness.setHand(player1, List.of(new AmbuscadeShaman()));
        harness.setHand(player2, List.of(new UltimatePrice()));
        addManaForNormalCast();
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, existing.getId());
        resolveAllTriggers();

        Permanent entering = findPermanent(player1, "Ambuscade Shaman");
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(6);
        harness.assertInGraveyard(player1, "Ambuscade Shaman");
    }

    private void addManaForNormalCast() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
