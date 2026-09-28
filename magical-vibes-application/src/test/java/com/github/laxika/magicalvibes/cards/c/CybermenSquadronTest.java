package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({CybermenSquadron.class, Ornithopter.class, GrizzlyBears.class})
class CybermenSquadronTest extends BaseCardTest {

    @Test
    @DisplayName("Grants myriad to nonlegendary artifact creatures you control")
    void grantsMyriadToNonlegendaryArtifactCreatures() {
        Player player3 = addOpponent("Charlie");
        addCreatureReady(player1, new CybermenSquadron());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        resolveMyriad(player2, ornithopter);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player3.getId());
    }

    @Test
    @DisplayName("The nonlegendary artifact source also has myriad")
    void sourceAlsoHasMyriad() {
        Player player3 = addOpponent("Charlie");
        Permanent squadron = addCreatureReady(player1, new CybermenSquadron());

        resolveMyriad(player2, squadron);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.isTapped()
                        && permanent.isAttacking()
                        && player3.getId().equals(permanent.getAttackTarget()));
    }

    @Test
    @DisplayName("Does not grant myriad to nonartifact creatures")
    void doesNotGrantMyriadToNonartifactCreatures() {
        addOpponent("Charlie");
        addCreatureReady(player1, new CybermenSquadron());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> declareAttackersAt(player2, bears));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void resolveMyriad(Player target, Permanent attacker) {
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackersAt(target, attacker);
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });
    }

    private void declareAttackersAt(Player target, Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player1, List.of(attackerIndex), Map.of(attackerIndex, target.getId()));
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
