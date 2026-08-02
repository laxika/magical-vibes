package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AetherizeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all attacking creatures to their owners' hands")
    void returnsAllAttackingCreatures() {
        Permanent ownAttacker = addAttacker(player1);
        Permanent opponentAttacker = addAttacker(player2);
        Permanent nonAttacker = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Aetherize()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownAttacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(nonAttacker);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getId().equals(ownAttacker.getCard().getId()))
                .hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getId().equals(opponentAttacker.getCard().getId()))
                .hasSize(1);
    }

    private Permanent addAttacker(Player controller) {
        Permanent attacker = addCreatureReady(controller, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(controller.equals(player1) ? player2.getId() : player1.getId());
        return attacker;
    }
}
