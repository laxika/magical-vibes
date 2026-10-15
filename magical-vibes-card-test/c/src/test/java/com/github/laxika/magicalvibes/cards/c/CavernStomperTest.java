package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GeologicalAppraiser;
import com.github.laxika.magicalvibes.cards.h.HermiticNautilus;
import com.github.laxika.magicalvibes.cards.h.HoverstonePilgrim;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CavernStomper.class, HoverstonePilgrim.class, GeologicalAppraiser.class, HermiticNautilus.class})
class CavernStomperTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield offers scry 2")
    void etbOffersScryTwo() {
        harness.castFromHand(player1, new CavernStomper(), "{4}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Activated ability prevents power 2 or less creatures from blocking this turn")
    void activatedAbilityRestrictsBlockersByPower() {
        Permanent stomper = addCreatureReady(player1, new CavernStomper());
        Permanent pilgrim = addCreatureReady(player2, new HoverstonePilgrim());

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        attack(stomper);
        assertThatThrownBy(() -> declareBlock(pilgrim, stomper))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or greater");
    }

    @Test
    @DisplayName("Activated ability still allows a creature with power 3 or greater to block")
    void activatedAbilityAllowsLargerBlockers() {
        Permanent stomper = addCreatureReady(player1, new CavernStomper());
        Permanent appraiser = addCreatureReady(player2, new GeologicalAppraiser());

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        attack(stomper);
        declareBlock(appraiser, stomper);

        assertThat(appraiser.isBlocking()).isTrue();
    }

    @Test
    void scryCanSplitCardsBetweenTopAndBottom() {
        HoverstonePilgrim first = new HoverstonePilgrim();
        GeologicalAppraiser second = new GeologicalAppraiser();
        HermiticNautilus third = new HermiticNautilus();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new CavernStomper(), "{4}{G}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
    }

    @Test
    void scryCanReorderBothCardsOnTop() {
        HoverstonePilgrim first = new HoverstonePilgrim();
        GeologicalAppraiser second = new GeologicalAppraiser();
        HermiticNautilus third = new HermiticNautilus();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new CavernStomper(), "{4}{G}{G}");
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
    }

    @Test
    void scryCanPutBothCardsOnBottomInChosenOrder() {
        HoverstonePilgrim first = new HoverstonePilgrim();
        GeologicalAppraiser second = new GeologicalAppraiser();
        HermiticNautilus third = new HermiticNautilus();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new CavernStomper(), "{4}{G}{G}");
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
    }

    @Test
    void scryWithOneCardLooksAtOnlyThatCard() {
        HoverstonePilgrim card = new HoverstonePilgrim();
        harness.setLibrary(player1, List.of(card));
        harness.castFromHand(player1, new CavernStomper(), "{4}{G}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(card);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
    }

    @Test
    void scryWithEmptyLibraryFinishesWithoutInput() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new CavernStomper(), "{4}{G}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof CavernStomper);
    }

    @Test
    void blockerPowerIsCheckedWhenBlockingRatherThanWhenAbilityResolves() {
        Permanent stomper = addCreatureReady(player1, new CavernStomper());
        Permanent nautilus = addCreatureReady(player2, new HermiticNautilus());
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        attack(stomper);
        declareBlock(nautilus, stomper);
        assertThat(nautilus.isBlocking()).isTrue();
    }

    @Test
    void smallCreatureCanBlockWithoutActivation() {
        Permanent stomper = addCreatureReady(player1, new CavernStomper());
        Permanent blocker = addCreatureReady(player2, new HoverstonePilgrim());

        attack(stomper);
        declareBlock(blocker, stomper);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void restrictionExpiresAtEndOfTurn() {
        Permanent stomper = addCreatureReady(player1, new CavernStomper());
        Permanent blocker = addCreatureReady(player2, new HoverstonePilgrim());
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        attack(stomper);
        declareBlock(blocker, stomper);
        assertThat(blocker.isBlocking()).isTrue();
    }

    private void attack(Permanent attacker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
