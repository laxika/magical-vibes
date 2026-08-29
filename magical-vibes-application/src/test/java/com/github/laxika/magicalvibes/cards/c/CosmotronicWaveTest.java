package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CosmotronicWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage only to opponents' creatures")
    void damagesOnlyOpponentsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCosmotronicWave();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponents' creatures can't block this turn")
    void preventsOpponentsCreaturesFromBlocking() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCosmotronicWave();

        assertThat(ownCreature.isCantBlockThisTurn()).isFalse();
        assertThat(opposingCreature.isCantBlockThisTurn()).isTrue();
    }

    private void castCosmotronicWave() {
        harness.setHand(player1, List.of(new CosmotronicWave()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
