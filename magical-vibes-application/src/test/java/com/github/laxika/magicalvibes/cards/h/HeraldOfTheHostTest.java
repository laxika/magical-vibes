package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GenerousGift;
import com.github.laxika.magicalvibes.cards.t.TeferiTemporalArchmage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldOfTheHost.class, GenerousGift.class, TeferiTemporalArchmage.class})
class HeraldOfTheHostTest extends BaseCardTest {

    @Test
    @DisplayName("Myriad creates a tapped and attacking copy for each other opponent")
    void myriadCreatesCopyForEachOtherOpponent() {
        Player player3 = addOpponent("Charlie");
        addCreatureReady(player1, new HeraldOfTheHost());

        declareAttackersAt(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);
        assertThat(gd.stack).as("stack after accepting myriad").extracting(e -> e.getDescription()).isEmpty();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player3.getId());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("Myriad does not create a copy when there is no other opponent")
    void myriadDoesNotCreateCopyInTwoPlayerGame() {
        addCreatureReady(player1, new HeraldOfTheHost());

        declareAttackersAt(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void myriadCopyMayBeDeclined() {
        addOpponent("Charlie");
        addCreatureReady(player1, new HeraldOfTheHost());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackersAt(player2);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
        });

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void myriadChoicesAreIndependentForEachOpponent() {
        addOpponent("Charlie");
        Player player4 = addOpponent("Dana");
        addCreatureReady(player1, new HeraldOfTheHost());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackersAt(player2);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
            harness.handleMayAbilityChosen(player1, true);
        });

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .singleElement().satisfies(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttacking()).isTrue();
                    assertThat(token.getAttackTarget()).isEqualTo(player4.getId());
                });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void myriadCopyMayAttackTheOtherOpponentsPlaneswalker() {
        Player player3 = addOpponent("Charlie");
        Permanent teferi = harness.addToBattlefieldAndReturn(player3, new TeferiTemporalArchmage());
        addCreatureReady(player1, new HeraldOfTheHost());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackersAt(player2);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, teferi.getId());
        });

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .singleElement().satisfies(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttacking()).isTrue();
                    assertThat(token.getAttackTarget()).isEqualTo(teferi.getId());
                });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void myriadCopiesHeraldEvenAfterItLeavesTheBattlefield() {
        Player player3 = addOpponent("Charlie");
        Permanent herald = addCreatureReady(player1, new HeraldOfTheHost());
        harness.setHand(player1, List.of(new GenerousGift()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackersAt(player2);
            harness.castAndResolveInstant(player1, 0, herald.getId());
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(herald);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        });

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Herald of the Host")).toList())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttacking()).isTrue();
                    assertThat(token.getAttackTarget()).isEqualTo(player3.getId());
                });
    }

    @Test
    void myriadExilesAllCopiesWithOneDelayedTriggerFromTheOriginalHerald() {
        addOpponent("Charlie");
        addOpponent("Dana");
        Permanent herald = addCreatureReady(player1, new HeraldOfTheHost());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackersAt(player2);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleMayAbilityChosen(player1, true);
        });

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(tokens);
        assertThat(gd.stack).singleElement().satisfies(trigger -> {
            assertThat(trigger.getSourcePermanentId()).isEqualTo(herald.getId());
            assertThat(trigger.getControllerId()).isEqualTo(player1.getId());
        });

        harness.withAutoStop(TurnStep.END_OF_COMBAT, harness::passBothPriorities);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContainAnyElementsOf(tokens);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(herald);
    }

    private void declareAttackersAt(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, target.getId()));
    }

    private Player addOpponent(String name) {
        Player opponent = new Player(UUID.randomUUID(), name);
        gd.playerIds.add(opponent.getId());
        gd.orderedPlayerIds.add(opponent.getId());
        gd.playerNames.add(name);
        gd.playerIdToName.put(opponent.getId(), name);
        gd.playerDecks.put(opponent.getId(), new ArrayList<>());
        gd.playerHands.put(opponent.getId(), new ArrayList<>());
        gd.playerGraveyards.put(opponent.getId(), new ArrayList<>());
        gd.playerBattlefields.put(opponent.getId(), new ArrayList<>());
        gd.playerManaPools.put(opponent.getId(), new ManaPool());
        gd.playerLifeTotals.put(opponent.getId(), 20);
        return opponent;
    }
}
