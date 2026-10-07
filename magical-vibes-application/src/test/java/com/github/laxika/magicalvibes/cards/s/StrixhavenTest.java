package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Strixhaven.class, DarkRitual.class, LightningBolt.class, GrizzlyBears.class,
        WrathOfGod.class, Counterspell.class})
class StrixhavenTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
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
        harness.addMana(player2, ManaColor.BLACK, 1);
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
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextETBTokenMultiTargetTrigger(gd));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownSpell.getId(), opposingSpell.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(opposingSpell.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(opposingSpell);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownSpell, invalidCard);
    }

    @Test
    void grantsDemonstrateToSorceriesCastByThePlanarController() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(entry -> entry.isCopy())
                .extracting(entry -> entry.getControllerId())
                .containsExactly(player1.getId(), player2.getId());
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
    }

    @Test
    void decliningDemonstrateCreatesNoCopiesForEitherPlayer() {
        Card spell = new DarkRitual();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void creatureSpellsDoNotGainDemonstrate() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
    }

    @Test
    void bothPlayersCanChooseNewTargetsForTheirDemonstrateCopies() {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());

        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 17);
    }

    @Test
    void demonstrateStillCopiesTheSpellAfterTheOriginalIsCountered() {
        Card original = new DarkRitual();
        harness.setHand(player1, List.of(original));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.castInstant(player2, 0, original.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(original);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(entry -> entry.isCopy())
                .extracting(entry -> entry.getControllerId())
                .containsExactly(player1.getId(), player2.getId());
    }

    @Test
    void chaosCanChooseNoTargetEvenWhenAnEligibleCardExists() {
        Card spell = new DarkRitual();
        harness.setGraveyard(player1, List.of(spell));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextETBTokenMultiTargetTrigger(gd));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chaosReturnsASorceryToItsOwnersHand() {
        Card spell = new WrathOfGod();
        harness.setGraveyard(player1, List.of(spell));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextETBTokenMultiTargetTrigger(gd));
        harness.handleMultiplePermanentsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
