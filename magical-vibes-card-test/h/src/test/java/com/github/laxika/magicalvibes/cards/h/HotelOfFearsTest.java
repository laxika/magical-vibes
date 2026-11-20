package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HotelOfFears.class, AirElemental.class, GrizzlyBears.class})
class HotelOfFearsTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlanechase() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Upkeep exiles the top card, loses life equal to its mana value and lets you play it this turn")
    void upkeepExilesTopCardLosesItsManaValueAndMayPlayIt() {
        gd.planechase.faceUp.add(new PlanarObject(new HotelOfFears(), gd.nextTimestamp()));
        Card topCard = new AirElemental();
        harness.setLibrary(player1, List.of(topCard));
        int lifeBefore = gd.getLife(player1.getId());

        harness.forceStep(TurnStep.UPKEEP);
        GameTestEngineContext.get().getBean(StepTriggerService.class).handleUpkeepTriggers(gd);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - topCard.getManaValue());
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(topCard.getId());
        assertThat(gd.exilePlayPermissionHolders()).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayCardsWithDuration(Duration.UNTIL_END_OF_TURN)).contains(topCard.getId());
    }

    @Test
    @DisplayName("Chaos chooses a color, adds devotion counters to the target and offers only other creatures to sacrifice")
    void chaosOffersOnlyCreaturesOtherThanTheTargetToSacrifice() {
        gd.planechase.faceUp.add(new PlanarObject(new HotelOfFears(), gd.nextTimestamp()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent spared = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        triggerChaosTargeting(target);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(fodder.getId(), spared.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spared).doesNotContain(fodder);
        assertThat(gd.chosenSpellColor).isNull();
    }

    @Test
    @DisplayName("The only other creature is sacrificed and the targeted creature survives")
    void sacrificesTheOnlyOtherCreatureNotTheTarget() {
        gd.planechase.faceUp.add(new PlanarObject(new HotelOfFears(), gd.nextTimestamp()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        triggerChaosTargeting(target);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("With no other creature nothing is sacrificed and the targeted creature keeps its counters")
    void targetedCreatureIsNeverSacrificed() {
        gd.planechase.faceUp.add(new PlanarObject(new HotelOfFears(), gd.nextTimestamp()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        triggerChaosTargeting(target);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Air Elemental");
    }

    private void triggerChaosTargeting(Permanent target) {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.beginTriggerChoices();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }
}
