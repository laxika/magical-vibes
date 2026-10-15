package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.b.BullCerodon;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
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

@CardUsed({StrongholdFurnace.class, Blaze.class, Panopticon.class, BullCerodon.class})
class StrongholdFurnaceTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void setupPlanarState() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(new Panopticon());
        gd.startingPlayerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private PlanarObject faceUp(Player controller) {
        PlanarObject object = new PlanarObject(new StrongholdFurnace(), gd.nextTimestamp());
        gd.planechase.controllerId = controller.getId();
        gd.planechase.faceUp.add(object);
        return object;
    }

    @Test
    void doublesAllDamageWhileFaceUp() {
        faceUp(player1);
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    void chaosAbilityDealsDoubledDamageToAnyTarget() {
        faceUp(player1);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSpellTargetTrigger(gd));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void doublesDamageToCreatures() {
        faceUp(player1);
        var target = harness.addToBattlefieldAndReturn(player2, new BullCerodon());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Bull Cerodon");
    }

    @Test
    void doublesDamageFromTheOtherPlayer() {
        faceUp(player1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player2, 0, 3, player1.getId());

        harness.assertLife(player1, 14);
    }

    @Test
    void chaosCanTargetACreature() {
        faceUp(player1);
        var target = harness.addToBattlefieldAndReturn(player2, new BullCerodon());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Bull Cerodon");
    }

    @Test
    void chaosStillResolvesAfterPlaneswalkingAwayWithoutDoubling() {
        faceUp(player1);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Blaze(), new Blaze()));
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, player2.getId());

        harness.inMutationScope(() -> planar.planeswalk(gd));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }
}
