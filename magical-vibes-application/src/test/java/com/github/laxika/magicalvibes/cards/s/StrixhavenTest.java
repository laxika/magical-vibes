package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Strixhaven.class, DarkRitual.class, LightningBolt.class, GrizzlyBears.class})
class StrixhavenTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Strixhaven(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void grantsDemonstrateToInstantAndSorcerySpellsOfBothPlayers() {
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(entry -> entry.isCopy())
                .extracting(entry -> entry.getControllerId())
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    @Test
    void chaosReturnsUpToOneInstantOrSorceryFromAnyGraveyard() {
        Card ownSpell = new DarkRitual();
        Card opposingSpell = new LightningBolt();
        Card invalidCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownSpell, invalidCard));
        harness.setGraveyard(player2, List.of(opposingSpell));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextETBTokenMultiTargetTrigger(gd));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownSpell.getId(), opposingSpell.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(opposingSpell.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(opposingSpell);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownSpell, invalidCard);
    }
}
