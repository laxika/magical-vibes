package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScionOfCalamity.class, FountainOfYouth.class, AngelicChorus.class, GrizzlyBears.class})
class ScionOfCalamityTest extends BaseCardTest {

    private Player player3;

    @Test
    @DisplayName("Myriad creates a tapped and attacking copy for another opponent")
    void myriadCreatesCopyAndExilesItAtEndOfCombat() {
        addThirdPlayer();
        Permanent scion = addCreatureReady(player1, new ScionOfCalamity());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent copy = findPermanents(player1, "Scion of Calamity").stream()
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
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scion);
    }

    @Test
    @DisplayName("Combat damage destroys an artifact or enchantment controlled by the damaged player")
    void combatDamageDestroysDamagedPlayersArtifactOrEnchantment() {
        Permanent scion = addCreatureReady(player1, new ScionOfCalamity());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        scion.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId(), enchantment.getId());
        assertThat(choice.validIds()).doesNotContain(ownArtifact.getId(), creature.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void myriadCanBeDeclined() {
        addThirdPlayer();
        Permanent scion = addCreatureReady(player1, new ScionOfCalamity());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
            harness.passBothPriorities();
        });

        assertThat(findPermanents(player1, "Scion of Calamity")).containsExactly(scion);
    }

    @Test
    void combatDamageCanDestroyAnEnchantment() {
        Permanent scion = addCreatureReady(player1, new ScionOfCalamity());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        scion.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertLife(player2, 15);
    }

    @Test
    void noTargetChoiceWhenDamagedPlayerHasNoArtifactOrEnchantment() {
        Permanent scion = addCreatureReady(player1, new ScionOfCalamity());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        scion.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetThatChangesControllerIsNotDestroyed() {
        Permanent scion = addCreatureReady(player1, new ScionOfCalamity());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        scion.setAttacking(true);

        resolveCombat();
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE,
                () -> harness.handlePermanentChosen(player1, artifact.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void myriadCopyDestroysOnlyAnArtifactControlledByItsDamagedPlayer() {
        addThirdPlayer();
        addCreatureReady(player1, new ScionOfCalamity());
        Permanent artifact = harness.addToBattlefieldAndReturn(player3, new FountainOfYouth());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });
        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertLife(player3, 15);
        harness.assertInGraveyard(player3, "Fountain of Youth");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
    }

    @Test
    void myriadCreatesNoCopiesInATwoPlayerGame() {
        Permanent scion = addCreatureReady(player1, new ScionOfCalamity());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Scion of Calamity")).containsExactly(scion);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void oneMyriadResolutionCreatesOneExileTriggerForAllItsCopies() {
        addThirdPlayer();
        addOpponent("Dana", "conn-4");
        Permanent scion = addCreatureReady(player1, new ScionOfCalamity());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });
        assertThat(findPermanents(player1, "Scion of Calamity")).hasSize(3);

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Scion of Calamity")).containsExactly(scion);
    }

    @Test
    void myriadExileTriggerRetainsOriginalSourceAndControllerAfterCopyChangesControl() {
        addThirdPlayer();
        Permanent scion = addCreatureReady(player1, new ScionOfCalamity());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });
        Permanent copy = findPermanents(player1, "Scion of Calamity").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(copy);
        gd.playerBattlefields.get(player3.getId()).add(copy);
        copy.setAttacking(false);
        copy.setAttackTarget(null);

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(scion.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player3.getId())).doesNotContain(copy);
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
