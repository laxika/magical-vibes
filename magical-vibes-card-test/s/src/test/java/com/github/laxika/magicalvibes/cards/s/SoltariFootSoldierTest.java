package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KnightOfDawn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoltariFootSoldier.class, KnightOfDawn.class, SoltariCrusader.class})
class SoltariFootSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("A non-shadow creature cannot block Soltari Foot Soldier")
    void cannotBeBlockedByNonShadowCreature() {
        addAttacker(new SoltariFootSoldier());
        addCreatureReady(player2, new KnightOfDawn());
        prepareDeclareBlockers();

        assertThatThrownBy(this::declareSingleBlocker)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A shadow creature can block Soltari Foot Soldier")
    void canBeBlockedByShadowCreature() {
        addAttacker(new SoltariFootSoldier());
        Permanent blocker = addCreatureReady(player2, new SoltariCrusader());
        prepareDeclareBlockers();

        declareSingleBlocker();

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Soltari Foot Soldier cannot block a non-shadow creature")
    void cannotBlockNonShadowCreature() {
        addAttacker(new KnightOfDawn());
        addCreatureReady(player2, new SoltariFootSoldier());
        prepareDeclareBlockers();

        assertThatThrownBy(this::declareSingleBlocker)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Soltari Foot Soldier can block a shadow creature")
    void canBlockShadowCreature() {
        addAttacker(new SoltariCrusader());
        Permanent blocker = addCreatureReady(player2, new SoltariFootSoldier());
        prepareDeclareBlockers();

        declareSingleBlocker();

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private void declareSingleBlocker() {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
