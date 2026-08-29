package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SporewebWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("When Sporeweb Weaver is dealt damage, its controller gains life and creates a Saproling")
    void dealtDamageGainsLifeAndCreatesSaproling() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player2, new SporewebWeaver());
        Permanent pinger = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        pinger.setSummoningSick(false);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pinger), null,
                weaver.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 21);

        List<Permanent> tokens = findPermanents(player2, "Saproling");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(tokens.getFirst().getCard().isToken()).isTrue();
    }
}
