package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Shiv.class, GrizzlyBears.class, Panopticon.class})
class ShivTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Shiv(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void allCreaturesCanUseShivsFirebreathingAbility() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gs.getEffectiveActivatedAbilities(gd, ownCreature)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingCreature)).hasSize(1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(ownCreature), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    void chaosCreatesAFlyingFiveFiveRedDragonForThePlanarController() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
        assertThat(findPermanents(player2, "Dragon")).isEmpty();
    }

    @Test
    void creaturesLoseTheGrantedAbilityWhenShivIsPlaneswalkedAway() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        assertThat(gs.getEffectiveActivatedAbilities(gd, creature)).hasSize(1);
        gd.planechase.deck.add(new Panopticon());

        harness.inMutationScope(() -> planar.planeswalk(gd));

        assertThat(gs.getEffectiveActivatedAbilities(gd, creature)).isEmpty();
    }

    @Test
    void opposingTappedSummoningSickCreatureCanActivateRepeatedlyUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setSummoningSick(true);
        creature.setTapped(true);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.activateAbility(player2, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void activatedFirebreathingStillResolvesAfterPlaneswalkingAway() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        gd.planechase.deck.add(new Panopticon());

        harness.inMutationScope(() -> planar.planeswalk(gd));
        resolveAllTriggers();

        assertThat(gs.getEffectiveActivatedAbilities(gd, creature)).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void chaosUsesItsTriggerControllerEvenAfterPlaneswalkingAway() {
        harness.forceActivePlayer(player2);
        gd.planechase.controllerId = player2.getId();
        harness.inMutationScope(() -> planar.chaos(gd));
        gd.planechase.deck.add(new Panopticon());
        harness.inMutationScope(() -> planar.planeswalk(gd));
        resolveAllTriggers();

        Permanent dragon = findPermanent(player2, "Dragon");
        assertThat(dragon.getCard().isToken()).isTrue();
        assertThat(dragon.getCard().getColors()).containsExactly(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }
}
