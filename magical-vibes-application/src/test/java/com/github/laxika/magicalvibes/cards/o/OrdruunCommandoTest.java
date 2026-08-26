package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrdruunCommando.class, ProdigalSorcerer.class})
class OrdruunCommandoTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 1 damage dealt to itself")
    void preventsNextDamageToItself() {
        Permanent commando = harness.addToBattlefieldAndReturn(player1, new OrdruunCommando());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, commando.getId());
        harness.passBothPriorities();

        assertThat(commando.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Ordruun Commando");
    }
}
