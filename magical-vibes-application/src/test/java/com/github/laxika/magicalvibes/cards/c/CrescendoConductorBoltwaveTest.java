package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Boltwave;
import com.github.laxika.magicalvibes.cards.b.BraveMeadowguard;
import com.github.laxika.magicalvibes.cards.m.MightOfTheMeek;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrescendoConductorBoltwave.class, Boltwave.class, BraveMeadowguard.class, MightOfTheMeek.class})
class CrescendoConductorBoltwaveTest extends BaseCardTest {

    @Test
    void becomesPreparedWhenYouConjure() {
        Permanent conductor = harness.addToBattlefieldAndReturn(player1, new CrescendoConductorBoltwave());

        harness.enterBattlefieldAndReturn(player1, new BraveMeadowguard());
        resolveAllTriggers();

        assertThat(conductor.isPrepared()).isTrue();
        assertThat(conductor.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .anySatisfy(card -> assertThat(card).isInstanceOf(MightOfTheMeek.class));
    }

    @Test
    void castingBoltwaveFromPreparationDealsThreeToEachOpponent() {
        Permanent conductor = harness.addToBattlefieldAndReturn(player1, new CrescendoConductorBoltwave());
        harness.enterBattlefieldAndReturn(player1, new BraveMeadowguard());
        resolveAllTriggers();
        UUID preparedSpellId = conductor.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castFromExile(player1, preparedSpellId);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(conductor.isPrepared()).isFalse();
        assertThat(conductor.getPreparedSpellCardId()).isNull();
    }
}
