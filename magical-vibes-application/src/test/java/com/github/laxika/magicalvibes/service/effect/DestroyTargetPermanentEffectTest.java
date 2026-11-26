package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastAmountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCreatureTypeWithPermanentsPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * {@link DestroyTargetPermanentEffect} over a set resolved through the engine: the destroy step records its
 * departures for "this way" readers without losing an earlier sacrifice step's, a set's filter is bound as the
 * effect applies (CR 608.2h), and "creatures target player controls" declares the player target.
 */
@CardUsed({GrizzlyBears.class, HillGiant.class, LlanowarElves.class, DarksteelMyr.class})
class DestroyTargetPermanentEffectTest extends BaseCardTest {

    @BeforeEach
    void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    private StackEntry resolve(Player controller, UUID targetId, CardEffect... effects) {
        StackEntry entry = TestStackDoor.triggered(new GrizzlyBears(), controller.getId(), "Destroy",
                List.of(effects), targetId, (UUID) null);
        gd.stack.add(entry);
        harness.passBothPriorities();
        return entry;
    }

    private static int life(GameData gameData, Player player) {
        return gameData.getLife(player.getId());
    }

    @Test
    @DisplayName("\"Destroy all creatures. You gain 1 life for each creature that died this way\" counts the set's deaths")
    void setDestroyRecordsItsDepartures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        int before = life(gd, player1);

        resolve(player1, null, new DestroyTargetPermanentEffect(Subjects.allCreatures()),
                new GainLifeEffect(AmountThisWay.died(StepKind.DESTROY)));

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(life(gd, player1)).isEqualTo(before + 2);
    }

    @Test
    @DisplayName("A destroy step between a sacrifice and \"the sacrificed creature's power\" keeps the sacrifice's departures")
    void destroyStepKeepsAnEarlierSacrificesDepartures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int before = life(gd, player1);

        resolve(player1, null,
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                DestroyTargetPermanentEffect.all(new PermanentIsCreaturePredicate()),
                new GainLifeEffect(AmountThisWay.sacrificedPower()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(life(gd, player1)).isEqualTo(before + 3);
    }

    @Test
    @DisplayName("\"Destroy each creature with power 3 or greater\" fixes the amount as it applies and spares the others")
    void setFilterAmountIsBound() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        resolve(player1, null, new DestroyTargetPermanentEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastAmountPredicate(new Fixed(3)))))));

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("\"Destroy all creatures that share a creature type with the sacrificed creature\" reads its types as it left")
    void setFilterSharedCreatureTypeIsBound() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        resolve(player1, null,
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                new DestroyTargetPermanentEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                        new PermanentSharesCreatureTypeWithPermanentsPredicate(
                                Subjects.departedThisWay(StepKind.SACRIFICE)))));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("\"Destroy all creatures target player controls\" declares a harmful player target")
    void creaturesTargetPlayerControlsDeclaresThePlayer() {
        DestroyTargetPermanentEffect destroy = new DestroyTargetPermanentEffect(
                Subjects.creaturesTargetPlayerControls());

        assertThat(destroy.targetSpec().harmful()).isTrue();
        assertThat(destroy.targetSpec().admits(com.github.laxika.magicalvibes.model.effect.TargetPredicate.Kind.PLAYER))
                .isTrue();
    }

    @Test
    @DisplayName("\"Destroy all creatures target player controls\" destroys only that player's creatures")
    void creaturesTargetPlayerControlsDestroysThatPlayersCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        resolve(player1, player2.getId(),
                new DestroyTargetPermanentEffect(Subjects.creaturesTargetPlayerControls()));

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A wipe records what it destroyed, their controllers and every departure, an indestructible one included")
    void wipeRecordsItsStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());

        StackEntry entry = resolve(player1, null, DestroyTargetPermanentEffect.all(new PermanentIsCreaturePredicate()));

        StepOutcome destroyed = entry.getResolutionResults().outcome(StepKind.DESTROY);
        assertThat(destroyed.objectIds()).containsExactlyInAnyOrder(bears.getId(), giant.getId());
        assertThat(destroyed.playerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());
        assertThat(entry.getResolutionResults().departures(StepKind.DESTROY))
                .extracting(DepartureResult::permanentId, DepartureResult::destroyed)
                .containsExactlyInAnyOrder(tuple(bears.getId(), true), tuple(giant.getId(), true),
                        tuple(myr.getId(), false));
        harness.assertOnBattlefield(player2, "Darksteel Myr");
    }

    @Test
    @DisplayName("\"Destroy all creatures target player controls\" with no player destroys nothing and records an empty step")
    void creaturesTargetPlayerControlsWithoutAPlayerDestroysNothing() {
        harness.addToBattlefield(player2, new HillGiant());

        StackEntry entry = resolve(player1, null,
                new DestroyTargetPermanentEffect(Subjects.creaturesTargetPlayerControls()));

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(entry.getResolutionResults().outcome(StepKind.DESTROY)).isEqualTo(StepOutcome.NONE);
    }
}
