package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.q.QueenAllenalOfRuadach;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.CounterType;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodbirthViper.class, Forest.class, JaceBeleren.class, ParallelLives.class,
        QueenAllenalOfRuadach.class})
class BroodbirthViperTest extends BaseCardTest {

    private Player player3;

    @Test
    @DisplayName("Myriad creates a tapped and attacking copy for another opponent")
    void myriadCreatesCopyForAnotherOpponentAndExilesItAtEndOfCombat() {
        addThirdPlayer();
        Permanent viper = addCreatureReady(player1, new BroodbirthViper());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent copy = findPermanents(player1, "Broodbirth Viper").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(copy.getId())
                        && action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            for (int attempts = 0; gd.currentStep != TurnStep.END_OF_COMBAT && attempts < 16; attempts++) {
                if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                    harness.handleMayAbilityChosen(player1, false);
                } else {
                    harness.passBothPriorities();
                }
            }
            assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(viper);
    }

    @Test
    @DisplayName("Combat damage to a player may draw a card")
    void mayDrawOnCombatDamageToPlayer() {
        Permanent viper = addCreatureReady(player1, new BroodbirthViper());
        viper.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The combat-damage draw may be declined")
    void mayDeclineDrawOnCombatDamageToPlayer() {
        Permanent viper = addCreatureReady(player1, new BroodbirthViper());
        viper.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Myriad may be declined without creating a token")
    void mayDeclineMyriad() {
        addThirdPlayer();
        Permanent viper = addCreatureReady(player1, new BroodbirthViper());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Broodbirth Viper")).containsExactly(viper);
    }

    @Test
    @DisplayName("Myriad creates no tokens in a two-player game")
    void noMyriadTokensWithoutAnotherOpponent() {
        Permanent viper = addCreatureReady(player1, new BroodbirthViper());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        });

        assertThat(findPermanents(player1, "Broodbirth Viper")).containsExactly(viper);
    }

    @Test
    @DisplayName("Attacking a planeswalker creates a myriad copy for the other opponent")
    void attackingPlaneswalkerStillCreatesMyriadCopy() {
        addThirdPlayer();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        addCreatureReady(player1, new BroodbirthViper());

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

        assertThat(findPermanents(player1, "Broodbirth Viper"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(copy -> {
                    assertThat(copy.isTapped()).isTrue();
                    assertThat(copy.isAttacking()).isTrue();
                    assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
                });
    }

    @Test
    @DisplayName("A myriad token may attack the other opponent's planeswalker")
    void myriadCopyMayAttackPlaneswalker() {
        addThirdPlayer();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player3, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        addCreatureReady(player1, new BroodbirthViper());

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

        assertThat(findPermanents(player1, "Broodbirth Viper"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(copy -> {
                    assertThat(copy.isTapped()).isTrue();
                    assertThat(copy.isAttacking()).isTrue();
                    assertThat(copy.getAttackTarget()).isEqualTo(planeswalker.getId());
                });
    }

    @Test
    @DisplayName("With Queen Allenal and Parallel Lives, the controller orders them before the myriad copy enters")
    void asksToOrderTokenReplacements() {
        attackUnderQueenAndParallelLives();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
        assertThat(myriadCopies()).isEmpty();
    }

    @Test
    @DisplayName("Applying Queen Allenal first doubles the copy and the Soldier, all attacking the other opponent")
    void queenFirstDoublesCopyAndSoldier() {
        attackUnderQueenAndParallelLives();

        chooseReplacement("Queen Allenal of Ruadach");

        assertThat(myriadCopies()).hasSize(2).allSatisfy(copy -> {
            assertThat(copy.isTapped()).isTrue();
            assertThat(copy.isAttacking()).isTrue();
            assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
        });
        assertThat(findPermanents(player1, "Soldier")).hasSize(2).allSatisfy(soldier -> {
            assertThat(soldier.isTapped()).isTrue();
            assertThat(soldier.isAttacking()).isTrue();
            assertThat(soldier.getAttackTarget()).isEqualTo(player3.getId());
        });
        List<UUID> exiledAtEndOfCombat = gd.getDelayedActions(DelayedPermanentAction.class).stream()
                .filter(action -> action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT)
                .map(DelayedPermanentAction::permanentId).toList();
        assertThat(exiledAtEndOfCombat).containsAll(myriadCopies().stream().map(Permanent::getId).toList());
    }

    @Test
    @DisplayName("Applying Parallel Lives first doubles the copy and adds one undoubled Soldier")
    void parallelLivesFirstAddsOneSoldier() {
        attackUnderQueenAndParallelLives();

        chooseReplacement("Parallel Lives");

        assertThat(myriadCopies()).hasSize(2)
                .allSatisfy(copy -> assertThat(copy.getAttackTarget()).isEqualTo(player3.getId()));
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Each doubled myriad copy chooses between that opponent and their planeswalkers")
    void eachDoubledCopyChoosesItsOwnTarget() {
        addThirdPlayer();
        Permanent otherPlaneswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        otherPlaneswalker.setCounterCount(CounterType.LOYALTY, 3);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player3, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.addToBattlefield(player1, new ParallelLives());
        Permanent viper = addCreatureReady(player1, new BroodbirthViper());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            int viperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(viper);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(viperIndex), Map.of(viperIndex, player2.getId()));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            PendingInteraction.PermanentChoice first =
                    (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
            assertThat(first.validIds()).containsExactlyInAnyOrder(player3.getId(), planeswalker.getId());
            harness.handlePermanentChosen(player1, planeswalker.getId());
            PendingInteraction.PermanentChoice second =
                    (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
            assertThat(second.validIds()).containsExactlyInAnyOrder(player3.getId(), planeswalker.getId());
            harness.handlePermanentChosen(player1, player3.getId());
            resolveAllTriggers();
        });

        assertThat(myriadCopies()).extracting(Permanent::getAttackTarget)
                .containsExactlyInAnyOrder(planeswalker.getId(), player3.getId());
    }

    private void attackUnderQueenAndParallelLives() {
        addThirdPlayer();
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player1, new ParallelLives());
        Permanent viper = addCreatureReady(player1, new BroodbirthViper());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(viper)));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
        });
    }

    private void chooseReplacement(String optionPrefix) {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> harness.handleListChoice(player1,
                choice.options().stream().filter(option -> option.startsWith(optionPrefix)).findFirst().orElseThrow()));
    }

    private List<Permanent> myriadCopies() {
        return findPermanents(player1, "Broodbirth Viper").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
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
