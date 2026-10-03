package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladeOfSelves.class, GrizzlyBears.class, GarrukWildspeaker.class, AnointedProcession.class})
class BladeOfSelvesTest extends BaseCardTest {

    private Player player3;

    @Test
    @DisplayName("Equip attaches Blade of Selves to a creature")
    void equipAttachesToCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature has myriad")
    void equippedCreatureHasMyriad() {
        addThirdPlayer();
        Permanent creature = addCreatureReady(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
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
    }

    @Test
    @DisplayName("Myriad creates no copies in a two-player game")
    void noCopiesWithOnlyDefendingOpponent() {
        Permanent creature = addCreatureReady(player1);
        addBladeReady(player1).setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("Myriad can be declined")
    void mayDeclineCopy() {
        addThirdPlayer();
        Permanent creature = addCreatureReady(player1);
        addBladeReady(player1).setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(creature);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("Unattached Blade does not grant myriad")
    void unattachedBladeDoesNotTrigger() {
        addThirdPlayer();
        Permanent creature = addCreatureReady(player1);
        addBladeReady(player1);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Attacking a planeswalker still creates a copy for the other opponent")
    void attackingPlaneswalkerCreatesCopy() {
        addThirdPlayer();
        Permanent creature = addCreatureReady(player1);
        addBladeReady(player1).setAttachedTo(creature.getId());
        Permanent garruk = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, garruk.getId()));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .allSatisfy(copy -> assertThat(copy.getAttackTarget()).isEqualTo(player3.getId()));
    }

    @Test
    @DisplayName("Myriad offers a choice when the other opponent controls a planeswalker")
    void mayAttackOtherOpponentsPlaneswalker() {
        addThirdPlayer();
        Permanent creature = addCreatureReady(player1);
        addBladeReady(player1).setAttachedTo(creature.getId());
        harness.addToBattlefield(player3, new GarrukWildspeaker());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.isAwaitingInput()).isTrue();
        });
    }

    @Test
    @DisplayName("All multiplied myriad copies attack the other opponent")
    void multipliedCopiesHaveAttackTargets() {
        addThirdPlayer();
        Permanent creature = addCreatureReady(player1);
        addBladeReady(player1).setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new AnointedProcession());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        List<Permanent> copies = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(copies).hasSize(2).allSatisfy(copy -> {
            assertThat(copy.isTapped()).isTrue();
            assertThat(copy.isAttacking()).isTrue();
            assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
        });
    }

    @Test
    @DisplayName("Two Blades grant two separate myriad triggers")
    void multipleMyriadInstancesTriggerSeparately() {
        addThirdPlayer();
        Permanent creature = addCreatureReady(player1);
        addBladeReady(player1).setAttachedTo(creature.getId());
        addBladeReady(player1).setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Myriad makes all per-opponent choices before creating any copies")
    void copiesEnterTogetherAfterAllChoices() {
        addThirdPlayer();
        addOpponent("Dana", "conn-4");
        Permanent creature = addCreatureReady(player1);
        addBladeReady(player1).setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(creature);

            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(3);
    }

    private Permanent addBladeReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BladeOfSelves());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void addThirdPlayer() {
        player3 = addOpponent("Charlie", "conn-3");
    }

    private Player addOpponent(String name, String connectionId) {
        UUID thirdPlayerId = UUID.randomUUID();
        Player opponent = new Player(thirdPlayerId, name);
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add(name);
        gd.playerIdToName.put(thirdPlayerId, name);
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection(connectionId), thirdPlayerId, name);
        return opponent;
    }
}
