package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AutonSoldier.class, ChoMannoRevolutionary.class, GrizzlyBears.class, JaceBeleren.class, SoulWarden.class})
class AutonSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("Copying a creature adds artifact and myriad and removes legendary")
    void copyingCreatureAppliesCopyExceptions() {
        Permanent auton = castAndCopy(new ChoMannoRevolutionary());

        assertThat(auton.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(auton.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(auton.getCard().getPower()).isEqualTo(2);
        assertThat(auton.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The copied creature's myriad creates an attacking token copy")
    void copiedCreatureHasMyriad() {
        addThirdPlayer();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent auton = castAndCopy(new GrizzlyBears());
        auton.setSummoningSick(false);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(auton)));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player3.getId());
    }

    private Permanent castAndCopy(Card target) {
        harness.castFromHand(player1, new AutonSoldier(), "{4}{U}{U}");
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, target);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetPermanent.getId());
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Auton Soldier"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Declining to copy leaves a zero-toughness creature that dies")
    void decliningCopyDies() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new AutonSoldier(), "{4}{U}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Auton Soldier");
        harness.assertInGraveyard(player1, "Auton Soldier");
    }

    @Test
    @DisplayName("With no creature to copy, Auton Soldier dies without a copy choice")
    void noCreatureToCopyDies() {
        harness.castFromHand(player1, new AutonSoldier(), "{4}{U}{U}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Auton Soldier");
        harness.assertInGraveyard(player1, "Auton Soldier");
    }

    @Test
    @DisplayName("Myriad creates no tokens when the defending player is the only opponent")
    void myriadInTwoPlayerGameCreatesNoTokens() {
        Permanent auton = castAndCopy(new GrizzlyBears());
        auton.setSummoningSick(false);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(auton)));
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(auton);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("The controller may decline to create a myriad token")
    void myriadTokenCanBeDeclined() {
        addThirdPlayer();
        Permanent auton = castAndCopy(new GrizzlyBears());
        auton.setSummoningSick(false);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(auton)));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(auton);
    }

    @Test
    @DisplayName("Myriad copies retain the copy exceptions and are exiled at end of combat")
    void myriadCopiesKeepExceptionsAndAreExiled() {
        addThirdPlayer();
        Permanent auton = castAndCopy(new ChoMannoRevolutionary());
        auton.setSummoningSick(false);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(auton)));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            for (int passes = 0; gd.currentStep != TurnStep.POSTCOMBAT_MAIN && passes < 20; passes++) {
                if (gd.interaction.activeInteraction() instanceof PendingInteraction.BlockerDeclaration blockers) {
                    gs.declareBlockers(gd, new Player(blockers.chooserId(),
                            gd.playerIdToName.get(blockers.chooserId())), List.of());
                } else {
                    harness.passBothPriorities();
                }
            }
        });
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auton).doesNotContain(token);
        harness.assertNotInGraveyard(player1, "Cho-Manno, Revolutionary");
    }

    @Test
    @DisplayName("Attacking a planeswalker still creates myriad tokens for other opponents")
    void attackingPlaneswalkerCreatesMyriadToken() {
        addThirdPlayer();
        Permanent auton = castAndCopy(new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 3);
        auton.setSummoningSick(false);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            int index = gd.playerBattlefields.get(player1.getId()).indexOf(auton);
            gs.declareAttackers(gd, player1, List.of(index), Map.of(index, planeswalker.getId()));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.isAttacking()).isTrue();
                    assertThat(token.getAttackTarget()).isEqualTo(player3.getId());
                });
    }

    private Player player3;

    @Test
    void myriadUsesLastControllerOfDepartedAttackedPlaneswalker() {
        addThirdPlayer();
        Permanent auton = castAndCopy(new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 3);
        auton.setSummoningSick(false);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            int index = gd.playerBattlefields.get(player1.getId()).indexOf(auton);
            gs.declareAttackers(gd, player1, List.of(index), Map.of(index, planeswalker.getId()));
            harness.inMutationScope(() -> {
                GameTestEngineContext.get().getBean(CreatureControlService.class).applyControlEffect(
                        gd, player3.getId(), planeswalker,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        EffectDuration.PERMANENT, null, "Control effect");
                harness.getPermanentRemovalService().removePermanentToExile(gd, planeswalker);
            });
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> assertThat(token.getAttackTarget()).isEqualTo(player2.getId()));
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
        harness.getSessionManager().registerPlayer(new FakeConnection(connectionId), thirdPlayerId, name);
        return opponent;
    }

    @Test
    @DisplayName("Myriad tokens for different opponents enter simultaneously and see each other enter")
    void myriadTokensEnterSimultaneously() {
        addThirdPlayer();
        addOpponent("Dana", "conn-4");
        Permanent auton = castAndCopy(new SoulWarden());
        resolveAllTriggers();
        auton.setSummoningSick(false);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(auton)));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        harness.assertLife(player1, 24);
    }

    @Test
    void decliningFinalOpponentStillCreatesAcceptedCopies() {
        addThirdPlayer();
        addOpponent("Dana", "conn-4");
        Permanent auton = castAndCopy(new SoulWarden());
        resolveAllTriggers();
        auton.setSummoningSick(false);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(auton)));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(permanent -> permanent.getCard().isToken());
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        harness.assertLife(player1, 21);
    }
}
