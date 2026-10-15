package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KorCastigator;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FortifiedRampart.class, KorCastigator.class})
class FortifiedRampartTest extends BaseCardTest {

    @Test
    void defenderPreventsAttacking() {
        Permanent rampart = addCreatureReady(player1, new FortifiedRampart());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rampart.isAttacking()).isFalse();
    }

    @Test
    void defenderCanBlockWhileSummoningSick() {
        addCreatureReady(player1, new KorCastigator());
        Permanent rampart = harness.addToBattlefieldAndReturn(player2, new FortifiedRampart());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Fortified Rampart");
        harness.assertOnBattlefield(player1, "Kor Castigator");
        assertThat(rampart.isTapped()).isFalse();
    }
}
