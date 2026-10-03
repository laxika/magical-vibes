package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TeferiTemporalArchmage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BansheeOfTheDreadChoir.class, Forest.class, TeferiTemporalArchmage.class})
class BansheeOfTheDreadChoirTest extends BaseCardTest {

    private Player player3;

    @Test
    @DisplayName("Myriad creates a tapped and attacking copy for another opponent")
    void myriadCreatesCopyForAnotherOpponentAndExilesItAtEndOfCombat() {
        addThirdPlayer();
        Permanent banshee = addCreatureReady(player1, new BansheeOfTheDreadChoir());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent copy = findPermanents(player1, "Banshee of the Dread Choir").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(banshee);
    }

    @Test
    @DisplayName("Combat damage makes the damaged player discard a card")
    void combatDamageTriggersDiscard() {
        Permanent banshee = addCreatureReady(player1, new BansheeOfTheDreadChoir());
        banshee.setAttacking(true);
        harness.setHand(player2, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Myriad may be declined")
    void myriadMayBeDeclined() {
        addThirdPlayer();
        Permanent banshee = addCreatureReady(player1, new BansheeOfTheDreadChoir());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(banshee);
    }

    @Test
    @DisplayName("Myriad creates no copies with only one opponent")
    void myriadCreatesNoCopiesInTwoPlayerGame() {
        Permanent banshee = addCreatureReady(player1, new BansheeOfTheDreadChoir());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(banshee);
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Combat damage to a player with an empty hand needs no discard choice")
    void combatDamageToEmptyHand() {
        Permanent banshee = addCreatureReady(player1, new BansheeOfTheDreadChoir());
        banshee.setAttacking(true);
        banshee.setAttackTarget(player2.getId());
        harness.setHand(player2, List.of());

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            resolveAllTriggers();
        });

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @CardUsed({TeferiTemporalArchmage.class})
    @DisplayName("Myriad still creates a copy when the original attacks a planeswalker")
    void myriadWhenAttackingPlaneswalker() {
        addThirdPlayer();
        addCreatureReady(player1, new BansheeOfTheDreadChoir());
        Permanent planeswalker = new Permanent(new TeferiTemporalArchmage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);

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

        assertThat(findPermanents(player1, "Banshee of the Dread Choir"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(copy -> {
                    assertThat(copy.isTapped()).isTrue();
                    assertThat(copy.isAttacking()).isTrue();
                    assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
                });
    }

    @Test
    @DisplayName("A myriad copy makes its own damaged opponent discard")
    void myriadCopyTriggersDiscardForItsDefender() {
        addThirdPlayer();
        addCreatureReady(player1, new BansheeOfTheDreadChoir());
        harness.setHand(player2, List.of());
        harness.setHand(player3, List.of(new Forest()));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.handleCardChosen(player3, 0);
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player3, "Forest");
        assertThat(gd.playerHands.get(player3.getId())).isEmpty();
        harness.assertLife(player2, 16);
        harness.assertLife(player3, 16);
    }

    @Test
    @DisplayName("Myriad exile uses the stack and leaves a response window")
    void exileAtEndOfCombatUsesStack() {
        addThirdPlayer();
        addCreatureReady(player1, new BansheeOfTheDreadChoir());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });
        Permanent copy = findPermanents(player1, "Banshee of the Dread Choir").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(copy);
        assertThat(gd.stack).isNotEmpty();
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
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
