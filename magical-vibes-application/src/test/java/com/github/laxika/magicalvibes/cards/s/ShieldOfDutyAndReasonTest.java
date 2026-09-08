package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldOfDutyAndReason.class, GrizzlyBears.class, PhantomWarrior.class})
class ShieldOfDutyAndReasonTest extends BaseCardTest {

    @Test
    void enchantedCreatureHasProtectionFromGreenAndBlue() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        castShield(host);
        Permanent greenSource = addCreatureReady(player2, new GrizzlyBears());
        Permanent blueSource = addCreatureReady(player2, new PhantomWarrior());

        assertThat(gqs.hasProtectionFromSource(gd, host, greenSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, host, blueSource)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, host, CardColor.RED)).isFalse();
    }

    @Test
    void protectionIsLostWhenShieldLeavesTheBattlefield() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castShield(host);
        Permanent greenSource = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasProtectionFromSource(gd, host, greenSource)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasProtectionFromSource(gd, host, greenSource)).isFalse();
    }

    private Permanent castShield(Permanent host) {
        harness.setHand(player1, java.util.List.of(new ShieldOfDutyAndReason()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getClass() == ShieldOfDutyAndReason.class)
                .findFirst()
                .orElseThrow();
    }
}
