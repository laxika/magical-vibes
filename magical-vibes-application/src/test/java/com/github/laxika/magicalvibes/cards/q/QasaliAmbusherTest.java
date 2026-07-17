package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QasaliAmbusherTest extends BaseCardTest {

    private Permanent attackController(com.github.laxika.magicalvibes.model.Player attacker,
                                       com.github.laxika.magicalvibes.model.Player defender) {
        Permanent creature = harness.addToBattlefieldAndReturn(attacker, new GrizzlyBears());
        creature.setAttacking(true);
        creature.setAttackTarget(defender.getId());
        return creature;
    }

    @Test
    @DisplayName("Casts for free with flash while a creature attacks you and you control a Forest and a Plains")
    void freeFlashCastWhenConditionMet() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        attackController(player2, player1);
        harness.setHand(player1, List.of(new QasaliAmbusher()));

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Qasali Ambusher");
        // No mana was spent — it was cast without paying its mana cost.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot free-cast when no creature is attacking you")
    void rejectedWhenNotAttacked() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new QasaliAmbusher()));

        assertThatThrownBy(() -> harness.castCreatureWithEvoke(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        // The rejected cast rewinds — the card stays in hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Qasali Ambusher");
    }

    @Test
    @DisplayName("Cannot free-cast without both a Forest and a Plains")
    void rejectedWithoutRequiredLands() {
        harness.addToBattlefield(player1, new Forest());
        attackController(player2, player1);
        harness.setHand(player1, List.of(new QasaliAmbusher()));

        assertThatThrownBy(() -> harness.castCreatureWithEvoke(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Qasali Ambusher");
    }
}
