package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mirrorweave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LegionLoyalty.class, GrizzlyBears.class, HillGiant.class, Mirrorweave.class})
class LegionLoyaltyTest extends BaseCardTest {

    private Player player3;

    @Test
    @DisplayName("Legion Loyalty gives creatures you control myriad")
    void creaturesYouControlGainMyriad() {
        addThirdPlayer();
        harness.addToBattlefield(player1, new LegionLoyalty());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent copy = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(copy.getId())
                        && action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
    }

    @Test
    @DisplayName("Myriad can be declined")
    void mayDeclineCopy() {
        addThirdPlayer();
        harness.addToBattlefield(player1, new LegionLoyalty());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(bear);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Myriad creates no copies in a two-player game")
    void noOtherOpponentMeansNoCopies() {
        harness.addToBattlefield(player1, new LegionLoyalty());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(bear);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Legion Loyalty does not grant myriad to an opponent's creatures")
    void opponentCreaturesDoNotGainMyriad() {
        addThirdPlayer();
        harness.addToBattlefield(player1, new LegionLoyalty());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player2, "Grizzly Bears")).containsExactly(bear);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Each instance of myriad creates its own copy")
    void multipleLoyaltiesTriggerSeparately() {
        addThirdPlayer();
        harness.addToBattlefield(player1, new LegionLoyalty());
        harness.addToBattlefield(player1, new LegionLoyalty());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(2));
            for (int i = 0; i < 2; i++) {
                resolveAllTriggers();
                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
                harness.handleMayAbilityChosen(player1, true);
            }
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(2)
                .allSatisfy(copy -> assertThat(copy.getAttackTarget()).isEqualTo(player3.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Removing Legion Loyalty after an attack does not remove the pending myriad trigger")
    void pendingTriggerSurvivesLossOfLoyalty() {
        addThirdPlayer();
        Permanent loyalty = harness.addToBattlefieldAndReturn(player1, new LegionLoyalty());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        gd.playerBattlefields.get(player1.getId()).remove(loyalty);
        gd.playerGraveyards.get(player1.getId()).add(loyalty.getCard());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(1);
    }

    @Test
    @DisplayName("Myriad exile uses the stack and leaves a response window at end of combat")
    void exileWaitsForDelayedTriggerToResolve() {
        addThirdPlayer();
        harness.addToBattlefield(player1, new LegionLoyalty());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });
        Permanent copy = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(copy);
        assertThat(gd.stack).isNotEmpty();
        harness.withAutoStop(TurnStep.END_OF_COMBAT, this::resolveAllTriggers);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
    }

    @Test
    @DisplayName("Myriad copies the attacker's copiable characteristics at resolution")
    void copiesCurrentCharacteristicsAfterMirrorweave() {
        addThirdPlayer();
        harness.addToBattlefield(player1, new LegionLoyalty());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new Mirrorweave()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.castAndResolveInstant(player1, 0, giant.getId());
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(1)
                .allSatisfy(copy -> {
                    assertThat(copy.getCard().getName()).isEqualTo("Hill Giant");
                    assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
                });
    }

    private void addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
    }
}
