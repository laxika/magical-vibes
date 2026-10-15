package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormwingEntity.class, Divination.class, Shock.class})
class StormwingEntityTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {2}{U} less after casting an instant or sorcery and scries 2 on entry")
    void reducedCostAndEnterTheBattlefieldScry() {
        harness.setHand(player1, List.of(new Divination(), new StormwingEntity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Cannot use the reduced cost without an instant or sorcery cast this turn")
    void fullCostRequiredWithoutPriorInstantOrSorcery() {
        harness.setHand(player1, List.of(new StormwingEntity()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prowess gives +1/+1 for casting a noncreature spell until end of turn")
    void prowessBoostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent stormwing = harness.addToBattlefieldAndReturn(player1, new StormwingEntity());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stormwing)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stormwing)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stormwing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stormwing)).isEqualTo(3);
    }

    @Test
    void instantEnablesReductionOfBothGenericAndBlueMana() {
        harness.setHand(player1, List.of(new Shock(), new StormwingEntity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Stormwing Entity")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    void reducedCostStillRequiresOneBlueMana() {
        harness.setHand(player1, List.of(new Shock(), new StormwingEntity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsInstantNeitherReducesCostNorTriggersProwess() {
        Permanent stormwing = harness.addToBattlefieldAndReturn(player1, new StormwingEntity());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, stormwing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stormwing)).isEqualTo(3);

        harness.setHand(player1, List.of(new StormwingEntity()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castingCreatureNeitherTriggersProwessNorEnablesReduction() {
        Permanent stormwing = harness.addToBattlefieldAndReturn(player1, new StormwingEntity());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new StormwingEntity(), new StormwingEntity()));
        harness.addMana(player1, ManaColor.BLUE, 9);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Stormwing Entity")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, stormwing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stormwing)).isEqualTo(3);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void prowessTriggersForEachNoncreatureSpellBeforeSpellResolves() {
        Permanent stormwing = harness.addToBattlefieldAndReturn(player1, new StormwingEntity());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stormwing)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        resolveAllTriggers();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stormwing)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, stormwing)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        resolveAllTriggers();
    }

    @Test
    void entryScryCanReorderTopAndBottomWithoutDrawing() {
        Shock first = new Shock();
        StormwingEntity second = new StormwingEntity();
        Shock third = new Shock();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new StormwingEntity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entryScryWorksWithOnlyOneCardInLibrary() {
        Shock remaining = new Shock();
        harness.setLibrary(player1, List.of(remaining));
        harness.setHand(player1, List.of(new StormwingEntity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(remaining);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.stack).isEmpty();
    }
}
