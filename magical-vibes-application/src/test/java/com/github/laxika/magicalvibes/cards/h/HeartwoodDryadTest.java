package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
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

@CardUsed({HeartwoodDryad.class, MoggFanatic.class, SoltariFootSoldier.class})
class HeartwoodDryadTest extends BaseCardTest {

    private void attacker(Card card) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
    }

    @Test
    @DisplayName("Heartwood Dryad can block a creature with shadow")
    void blocksShadowAttacker() {
        Permanent dryad = addCreatureReady(player2, new HeartwoodDryad());
        attacker(new SoltariFootSoldier());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(dryad.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Heartwood Dryad still blocks creatures without shadow")
    void blocksNormalAttacker() {
        Permanent dryad = addCreatureReady(player2, new HeartwoodDryad());
        attacker(new MoggFanatic());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(dryad.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without the ability still can't block a creature with shadow")
    void plainBlockerCannotBlockShadow() {
        addCreatureReady(player2, new MoggFanatic());
        attacker(new SoltariFootSoldier());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Heartwood Dryad does not gain shadow while attacking")
    void canBeBlockedByNormalCreature() {
        attacker(new HeartwoodDryad());
        Permanent blocker = addCreatureReady(player2, new MoggFanatic());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
