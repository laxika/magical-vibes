package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WarWingSiren;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BassaraTowerArcher.class, WarWingSiren.class, Shock.class})
class BassaraTowerArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new WarWingSiren());
        attacker.setAttacking(true);

        Permanent archer = addCreatureReady(player2, new BassaraTowerArcher());

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(archer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot be targeted by an opponent's spell")
    void cannotBeTargetedByOpponentSpell() {
        Permanent archer = addCreatureReady(player1, new BassaraTowerArcher());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, archer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be targeted and destroyed by its controller's spell")
    void canBeTargetedByControllerSpell() {
        Permanent archer = addCreatureReady(player1, new BassaraTowerArcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, archer.getId());

        harness.assertNotOnBattlefield(player1, "Bassara Tower Archer");
        harness.assertInGraveyard(player1, "Bassara Tower Archer");
    }
}
