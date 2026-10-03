package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KingSolomonsFrogs;
import com.github.laxika.magicalvibes.cards.q.QuantumReduction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CouncilOfReeds.class, KingSolomonsFrogs.class, QuantumReduction.class})
class CouncilOfReedsTest extends BaseCardTest {

    @Test
    @DisplayName("Duplicate Council of Reeds creatures survive the legend rule")
    void duplicateCouncilCreaturesSurvive() {
        harness.addToBattlefield(player1, new CouncilOfReeds());
        harness.addToBattlefield(player1, new CouncilOfReeds());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not exempt duplicate legendary noncreature permanents")
    void doesNotExemptNoncreatures() {
        harness.addToBattlefield(player1, new CouncilOfReeds());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KingSolomonsFrogs());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KingSolomonsFrogs());

        harness.runStateBasedActions();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Creates a token copy at the beginning of combat after a noncreature spell")
    void createsTokenCopyAfterNoncreatureSpell() {
        Permanent council = harness.addToBattlefieldAndReturn(player1, new CouncilOfReeds());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent opponentCouncil = harness.addToBattlefieldAndReturn(player2, new CouncilOfReeds());
        castReduction(opponentCouncil);

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Council of Reeds")).contains(council).hasSize(2);
        assertThat(findPermanents(player1, "Council of Reeds"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a token copy after only a creature spell")
    void doesNotCreateTokenAfterCreatureSpell() {
        harness.addToBattlefield(player1, new CouncilOfReeds());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CouncilOfReeds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Council of Reeds")).hasSize(2);
        assertThat(findPermanents(player1, "Council of Reeds"))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void doesNotTriggerWithoutCastingASpell() {
        harness.addToBattlefield(player1, new CouncilOfReeds());

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Council of Reeds")).hasSize(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new CouncilOfReeds());
        Permanent opponentCouncil = harness.addToBattlefieldAndReturn(player2, new CouncilOfReeds());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castReduction(opponentCouncil);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Council of Reeds")).hasSize(1);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotQualify() {
        harness.addToBattlefield(player1, new CouncilOfReeds());
        Permanent opponentCouncil = harness.addToBattlefieldAndReturn(player2, new CouncilOfReeds());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new QuantumReduction()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.clearPriorityPassed();
        harness.castInstantWithSacrifices(player2, 0, opponentCouncil.getId(),
                List.of(opponentCouncil.getId()));
        harness.passBothPriorities();

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Council of Reeds")).hasSize(1);
    }

    @Test
    void lostAbilitiesStopCombatTrigger() {
        Permanent council = harness.addToBattlefieldAndReturn(player1, new CouncilOfReeds());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castReduction(council);

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Council of Reeds")).hasSize(1);
    }

    @Test
    void lostAbilitiesRestoreLegendRule() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CouncilOfReeds());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CouncilOfReeds());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castReduction(first);
        castReduction(second);

        harness.runStateBasedActions();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());
    }

    @Test
    void doesNotExemptOpponentsCreatures() {
        harness.addToBattlefield(player1, new CouncilOfReeds());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CouncilOfReeds());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CouncilOfReeds());
        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new QuantumReduction());
        firstAura.setAttachedTo(first.getId());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new QuantumReduction());
        secondAura.setAttachedTo(second.getId());

        harness.runStateBasedActions();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());
    }

    @Test
    void tokenCopiesCreateFurtherCopiesInLaterCombat() {
        harness.addToBattlefield(player1, new CouncilOfReeds());
        Permanent opponentCouncil = harness.addToBattlefieldAndReturn(player2, new CouncilOfReeds());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castReduction(opponentCouncil);
        advanceToBeginningOfCombat();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Council of Reeds")).hasSize(2);

        advanceToBeginningOfCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Council of Reeds")).hasSize(4);
    }

    @Test
    void spellCastBeforeCouncilEntersStillQualifies() {
        Permanent opponentCouncil = harness.addToBattlefieldAndReturn(player2, new CouncilOfReeds());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castReduction(opponentCouncil);
        harness.addToBattlefield(player1, new CouncilOfReeds());

        advanceToBeginningOfCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Council of Reeds")).hasSize(2);
    }

    @Test
    void copyIsCreatedEvenIfSourceLeavesBeforeResolution() {
        Permanent council = harness.addToBattlefieldAndReturn(player1, new CouncilOfReeds());
        Permanent opponentCouncil = harness.addToBattlefieldAndReturn(player2, new CouncilOfReeds());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castReduction(opponentCouncil);
        advanceToBeginningOfCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(council);
        gd.playerGraveyards.get(player1.getId()).add(council.getCard());

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Council of Reeds")).hasSize(1)
                .allMatch(permanent -> permanent.getCard().isToken());
    }

    private void castReduction(Permanent target) {
        harness.setHand(player1, List.of(new QuantumReduction()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.clearPriorityPassed();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
