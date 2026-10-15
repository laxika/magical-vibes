package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElephantMandrill.class, FountainOfYouth.class})
class ElephantMandrillTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each player creates a Food token")
    void eachPlayerCreatesFoodOnEntry() {
        castElephantMandrill();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player2, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("At the beginning of combat, gets +1/+1 for each artifact opponents control")
    void boostsForOpponentArtifacts() {
        Permanent elephantMandrill = harness.addToBattlefieldAndReturn(player1, new ElephantMandrill());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, elephantMandrill)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elephantMandrill)).isEqualTo(4);
    }

    @Test
    @DisplayName("The combat boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent elephantMandrill = harness.addToBattlefieldAndReturn(player1, new ElephantMandrill());
        harness.addToBattlefield(player2, new FountainOfYouth());

        advanceToCombatAndResolve(player1);
        assertThat(gqs.getEffectivePower(gd, elephantMandrill)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elephantMandrill)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephantMandrill)).isEqualTo(2);
    }

    @Test
    @DisplayName("Food can be sacrificed immediately to gain three life")
    void foodAbilityPaysCostsAndGainsLife() {
        castElephantMandrill();
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        for (Player player : List.of(player1, player2)) {
            Permanent food = findPermanent(player, "Food");
            harness.addMana(player, ManaColor.COLORLESS, 2);
            int index = gd.playerBattlefields.get(player.getId()).indexOf(food);
            harness.activateAbility(player, index, null, null);

            assertThat(findPermanents(player, "Food")).isEmpty();
            assertThat(gd.playerLifeTotals.get(player.getId())).isEqualTo(10);
            resolveAllTriggers();

            assertThat(gd.playerLifeTotals.get(player.getId())).isEqualTo(13);
        }
    }

    @Test
    @DisplayName("The opponent's Food counts, but your own Food does not")
    void countsOpponentFoodOnly() {
        castElephantMandrill();
        Permanent elephantMandrill = findPermanent(player1, "Elephant-Mandrill");

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, elephantMandrill)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elephantMandrill)).isEqualTo(3);
    }

    @Test
    @DisplayName("There is no boost when opponents control only nonartifact creatures")
    void doesNotCountOpponentNonartifacts() {
        Permanent elephantMandrill = harness.addToBattlefieldAndReturn(player1, new ElephantMandrill());
        harness.addToBattlefield(player2, new ElephantMandrill());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, elephantMandrill)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephantMandrill)).isEqualTo(2);
    }

    @Test
    @DisplayName("The combat ability does not trigger on an opponent's turn")
    void doesNotBoostDuringOpponentsCombat() {
        castElephantMandrill();
        Permanent elephantMandrill = findPermanent(player1, "Elephant-Mandrill");

        advanceToCombatAndResolve(player2);

        assertThat(gqs.getEffectivePower(gd, elephantMandrill)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephantMandrill)).isEqualTo(2);
    }

    @Test
    @DisplayName("Artifacts are counted when the combat ability resolves")
    void countsArtifactsAtResolution() {
        castElephantMandrill();
        Permanent elephantMandrill = findPermanent(player1, "Elephant-Mandrill");
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);

        Permanent food = findPermanent(player2, "Food");
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(food), null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elephantMandrill)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephantMandrill)).isEqualTo(2);
    }

    @Test
    @DisplayName("The resolved boost stays after an opponent sacrifices their artifact")
    void resolvedBoostDoesNotRecountArtifacts() {
        castElephantMandrill();
        Permanent elephantMandrill = findPermanent(player1, "Elephant-Mandrill");
        advanceToCombatAndResolve(player1);

        Permanent food = findPermanent(player2, "Food");
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(food), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Food")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, elephantMandrill)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elephantMandrill)).isEqualTo(3);
    }

    private void castElephantMandrill() {
        harness.setHand(player1, List.of(new ElephantMandrill()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }
}
