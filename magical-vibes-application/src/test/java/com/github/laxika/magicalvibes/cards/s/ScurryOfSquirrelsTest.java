package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScurryOfSquirrels.class, GrizzlyBears.class})
class ScurryOfSquirrelsTest extends BaseCardTest {

    @Test
    @DisplayName("Each of the two Myriad abilities creates a tapped and attacking copy")
    void doubleMyriadCreatesTwoCopies() {
        Player player3 = addOpponent("Charlie");
        Permanent scurry = addCreatureReady(player1, new ScurryOfSquirrels());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackersAt(player2, scurry);
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        List<Permanent> copies = findPermanents(player1, "Scurry of Squirrels").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).allSatisfy(copy -> {
            assertThat(copy.isTapped()).isTrue();
            assertThat(copy.isAttacking()).isTrue();
            assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
        });

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Scurry of Squirrels").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage puts a +1/+1 counter on the chosen creature you control")
    void combatDamagePutsCounterOnTargetCreatureYouControl() {
        Permanent scurry = addCreatureReady(player1, new ScurryOfSquirrels());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        scurry.setAttacking(true);

        resolveCombat();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage trigger does not target an opponent's creature")
    void combatDamageCannotTargetOpponentCreature() {
        Permanent scurry = addCreatureReady(player1, new ScurryOfSquirrels());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        scurry.setAttacking(true);

        resolveCombat();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).doesNotContain(opponentCreature.getId());
    }

    private void declareAttackersAt(Player target, Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player1, List.of(attackerIndex),
                java.util.Map.of(attackerIndex, target.getId()));
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
        gd.playerCommandZones.put(opponent.getId(), new ArrayList<>());
        gd.playerManaPools.put(opponent.getId(), new ManaPool());
        gd.playerLifeTotals.put(opponent.getId(), 20);
        return opponent;
    }
}
