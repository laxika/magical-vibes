package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.u.UginTheIneffable;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChitteringDispatcher.class, LightningBolt.class, UginTheIneffable.class})
class ChitteringDispatcherTest extends BaseCardTest {

    @Test
    void leavesBattlefieldCreatesEldraziSpawn() {
        harness.addToBattlefield(player1, new ChitteringDispatcher());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Chittering Dispatcher"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chittering Dispatcher");
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    void myriadCreatesTappedAttackingCopyForAnotherOpponent() {
        addThirdPlayer();
        Permanent dispatcher = addCreatureReady(player1, new ChitteringDispatcher());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent copy = findPermanents(player1, "Chittering Dispatcher").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dispatcher);
    }

    @Test
    void spawnCanBeSacrificedImmediatelyForColorlessMana() {
        harness.addToBattlefield(player1, new ChitteringDispatcher());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Chittering Dispatcher"));
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(spawn), null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spawn);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(manaBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void myriadCanBeDeclined() {
        addThirdPlayer();
        Permanent dispatcher = addCreatureReady(player1, new ChitteringDispatcher());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Chittering Dispatcher")).containsExactly(dispatcher);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    void myriadCreatesNoCopiesWithOnlyOneOpponent() {
        Permanent dispatcher = addCreatureReady(player1, new ChitteringDispatcher());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        });

        assertThat(findPermanents(player1, "Chittering Dispatcher")).containsExactly(dispatcher);
    }

    @Test
    void exilingMyriadCopyAtEndOfCombatCreatesSpawn() {
        addThirdPlayer();
        Permanent dispatcher = addCreatureReady(player1, new ChitteringDispatcher());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });
        assertThat(findPermanents(player1, "Chittering Dispatcher")).hasSize(2);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();

        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        harness.withAutoStop(TurnStep.END_OF_COMBAT, this::resolveAllTriggers);

        assertThat(findPermanents(player1, "Chittering Dispatcher")).containsExactly(dispatcher);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void myriadExileUsesTheStackAtBeginningOfEndOfCombat() {
        addThirdPlayer();
        addCreatureReady(player1, new ChitteringDispatcher());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
        assertThat(findPermanents(player1, "Chittering Dispatcher")).hasSize(2);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    @CardUsed({UginTheIneffable.class})
    void attackingPlaneswalkerStillCreatesMyriadCopyForOtherOpponent() {
        addThirdPlayer();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new UginTheIneffable());
        addCreatureReady(player1, new ChitteringDispatcher());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Chittering Dispatcher"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(copy -> {
                    assertThat(copy.isTapped()).isTrue();
                    assertThat(copy.isAttacking()).isTrue();
                    assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
                });
    }

    @Test
    @CardUsed({UginTheIneffable.class})
    void myriadCopyCanAttackOtherOpponentsPlaneswalker() {
        addThirdPlayer();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player3, new UginTheIneffable());
        planeswalker.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 5);
        addCreatureReady(player1, new ChitteringDispatcher());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            PendingInteraction.PermanentChoice choice =
                    (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
            assertThat(choice.validIds()).containsExactlyInAnyOrder(player3.getId(), planeswalker.getId());
            harness.handlePermanentChosen(player1, planeswalker.getId());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Chittering Dispatcher"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(copy -> assertThat(copy.getAttackTarget()).isEqualTo(planeswalker.getId()));
    }

    @Test
    void myriadCopiesEnterTogetherAfterAllOpponentChoices() {
        addThirdPlayer();
        Player player4 = addOpponent("Dana", "conn-4");
        Permanent dispatcher = addCreatureReady(player1, new ChitteringDispatcher());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(findPermanents(player1, "Chittering Dispatcher")).containsExactly(dispatcher);
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Chittering Dispatcher"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .extracting(Permanent::getAttackTarget)
                .containsExactlyInAnyOrder(player3.getId(), player4.getId());
    }

    private Player player3;

    private void addThirdPlayer() {
        player3 = addOpponent("Charlie", "conn-3");
    }

    private Player addOpponent(String name, String connectionId) {
        UUID opponentId = UUID.randomUUID();
        Player opponent = new Player(opponentId, name);
        gd.playerIds.add(opponentId);
        gd.orderedPlayerIds.add(opponentId);
        gd.playerNames.add(name);
        gd.playerIdToName.put(opponentId, name);
        gd.playerDecks.put(opponentId, new ArrayList<>());
        gd.playerHands.put(opponentId, new ArrayList<>());
        gd.playerBattlefields.put(opponentId, new ArrayList<>());
        gd.playerGraveyards.put(opponentId, new ArrayList<>());
        gd.playerCommandZones.put(opponentId, new ArrayList<>());
        gd.playerManaPools.put(opponentId, new ManaPool());
        gd.playerLifeTotals.put(opponentId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection(connectionId), opponentId, name);
        return opponent;
    }
}
