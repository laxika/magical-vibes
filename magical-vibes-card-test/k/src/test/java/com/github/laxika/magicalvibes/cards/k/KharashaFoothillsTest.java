package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KharashaFoothills.class, GrizzlyBears.class, AirElemental.class, GarrukWildspeaker.class})
class KharashaFoothillsTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new KharashaFoothills(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void chaosDealsDamageEqualToTheNumberOfCreaturesSacrificed() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new AirElemental());

        harness.inMutationScope(() -> planar.chaos(gd));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class))
                .isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent ->
                permanent.getId().equals(first.getId()) || permanent.getId().equals(second.getId()));
    }

    @Test
    void choosingNoCreaturesDealsNoDamageToThePreviouslyChosenTarget() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new AirElemental());

        harness.inMutationScope(() -> planar.chaos(gd));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void attackingAPlayerCreatesAnOptionalCopyForTheOtherOpponentAndExilesItAtEndStep() {
        Player otherOpponent = addOpponent();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        Permanent copy = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(otherOpponent.getId());

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(copy);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker).doesNotContain(copy);
    }

    @Test
    void attackingAPlayerAllowsDecliningTheCopy() {
        addOpponent();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(attacker);
    }

    @Test
    void attackingAPlaneswalkerDoesNotTriggerTheCopyAbility() {
        addOpponent();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
            assertThat(gd.stack).isEmpty();
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isNull();
        });

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(attacker);
    }

    private Player addOpponent() {
        UUID id = UUID.randomUUID();
        Player opponent = new Player(id, "Charlie");
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, "Charlie");
        return opponent;
    }
}
