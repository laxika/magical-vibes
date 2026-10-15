package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Aetherize.class, DiscipleOfTheOldWays.class})
class AetherizeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all attacking creatures to their owners' hands")
    void returnsAllAttackingCreatures() {
        Permanent firstAttacker = addAttacker(player2);
        Permanent opponentAttacker = addAttacker(player2);
        Permanent nonAttacker = addCreatureReady(player2, new DiscipleOfTheOldWays());
        Permanent blocker = addCreatureReady(player1, new DiscipleOfTheOldWays());
        blocker.setBlocking(true);

        castAetherize();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(nonAttacker);
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getId().equals(firstAttacker.getCard().getId()))
                .hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getId().equals(opponentAttacker.getCard().getId()))
                .hasSize(1);
    }

    private Permanent addAttacker(Player controller) {
        Permanent attacker = addCreatureReady(controller, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        attacker.setAttackTarget(controller.equals(player1) ? player2.getId() : player1.getId());
        return attacker;
    }

    @Test
    @DisplayName("Returns an attacking creature to its owner rather than its controller")
    void returnsStolenAttackerToOwner() {
        DiscipleOfTheOldWays creature = new DiscipleOfTheOldWays();
        creature.setOwnerId(player1.getId());
        Permanent attacker = addCreatureReady(player2, creature);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        castAetherize();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(attacker.getCard());
    }

    @Test
    @DisplayName("Resolves with no attackers and leaves nonattacking creatures alone")
    void resolvesWithoutAttackers() {
        Permanent creature = addCreatureReady(player2, new DiscipleOfTheOldWays());

        castAetherize();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Aetherize");
    }

    @Test
    @DisplayName("Checks whether creatures are attacking when the spell resolves")
    void ignoresCreatureRemovedFromCombatBeforeResolution() {
        Permanent attacker = addAttacker(player2);

        castAetherize();
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(attacker);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(attacker.getCard());
    }

    private void castAetherize() {
        harness.setHand(player1, List.of(new Aetherize()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstant(player1, 0);
    }
}
