package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ketria.class, GrizzlyBears.class, Forest.class, Shock.class, Pacifism.class})
class KetriaTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Ketria(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @ParameterizedTest
    @CsvSource({
            "Put a vigilance counter on it, VIGILANCE",
            "Put a menace counter on it, MENACE",
            "Put a trample counter on it, TRAMPLE"
    })
    @DisplayName("Planeswalking to Ketria puts the chosen counter on a creature you control")
    void planeswalkTriggerPutsChosenCounterOnYourCreature(String mode, CounterType counterType) {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        triggerAndChooseTarget(EffectSlot.PLANESWALK_TO_TRIGGERED, target);
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        harness.handleListChoice(player1, mode);

        assertThat(target.getCounterCount(counterType)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(counterType)).isZero();
    }

    @Test
    @DisplayName("Ketria's upkeep trigger targets a creature you control")
    void upkeepTriggerTargetsYourCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).containsExactly(target.getId())
                .doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a menace counter on it");

        assertThat(target.getCounterCount(CounterType.MENACE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chaos exiles until a nonland permanent and puts it onto the battlefield")
    void chaosPutsFoundPermanentOntoBattlefield() {
        Card shock = new Shock();
        Card forest = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, forest, creature));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(shock.getId(), forest.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(creature.getId());
    }

    @Test
    @DisplayName("Chaos can put the found permanent into your hand")
    void chaosPutsFoundPermanentIntoHand() {
        Card shock = new Shock();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, creature));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(shock.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .doesNotContain(creature.getId());
    }

    @Test
    void counterTriggerDoesNotChooseACounterAfterTargetLeavesBattlefield() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        PlanarObject plane = gd.planechase.faceUp.getFirst();
        harness.inMutationScope(() -> planar.trigger(gd, plane,
                EffectSlot.PLANESWALK_TO_TRIGGERED, player1.getId()));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setGraveyard(player1, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getCounterCount(CounterType.VIGILANCE)).isZero();
        assertThat(target.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isZero();
    }

    @Test
    void chaosStopsAtFirstNonlandPermanent() {
        Card first = new GrizzlyBears();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(first, remaining));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void chaosExilesEntireLibraryWhenNoNonlandPermanentExists() {
        Card shock = new Shock();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(shock, forest));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock, forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chaosWithEmptyLibraryDoesNotRequestADestination() {
        harness.setLibrary(player1, List.of());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chaosAuraEntersAttachedToChosenCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Card aura = new Pacifism();
        harness.setLibrary(player1, List.of(aura));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanent(player1, "Pacifism").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void chaosAuraStaysExiledWhenThereIsNothingItCanEnchant() {
        Card aura = new Pacifism();
        harness.setLibrary(player1, List.of(aura));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(aura);
        harness.assertNotOnBattlefield(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
        harness.assertNotInHand(player1, "Pacifism");
    }

    private void triggerAndChooseTarget(EffectSlot slot, Permanent target) {
        PlanarObject plane = gd.planechase.faceUp.getFirst();
        harness.inMutationScope(() -> planar.trigger(gd, plane, slot, player1.getId()));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
