package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnaDisciple;
import com.github.laxika.magicalvibes.cards.c.CetaDisciple;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShieldOfDutyAndReason.class, PhyrexianRager.class, AnaDisciple.class, CetaDisciple.class})
class ShieldOfDutyAndReasonTest extends BaseCardTest {

    @Test
    void enchantedCreatureHasProtectionFromGreenAndBlue() {
        Permanent host = addCreatureReady(player1, new PhyrexianRager());
        castShield(host);
        Permanent greenSource = addCreatureReady(player2, new AnaDisciple());
        Permanent blueSource = addCreatureReady(player2, new CetaDisciple());
        Permanent otherCreature = addCreatureReady(player1, new PhyrexianRager());

        assertThat(gqs.hasProtectionFromSource(gd, host, greenSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, host, blueSource)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, host, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFromSource(gd, otherCreature, greenSource)).isFalse();
    }

    @Test
    void protectionPreventsGreenAndBlueAbilitiesFromTargetingEnchantedCreature() {
        Permanent host = addCreatureReady(player1, new PhyrexianRager());
        castShield(host);
        Permanent greenSource = addCreatureReady(player2, new AnaDisciple());
        Permanent blueSource = addCreatureReady(player2, new CetaDisciple());

        harness.addMana(player2, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(greenSource), 0, null, host.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");

        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(blueSource), 0, null, host.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void protectedCreatureCannotBeBlockedByGreenCreature() {
        Permanent host = addCreatureReady(player1, new PhyrexianRager());
        castShield(host);
        Permanent greenBlocker = addCreatureReady(player2, new AnaDisciple());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(host)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(greenBlocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(host)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void protectionIsLostWhenShieldLeavesTheBattlefield() {
        Permanent host = addCreatureReady(player1, new PhyrexianRager());
        Permanent aura = castShield(host);
        Permanent greenSource = addCreatureReady(player2, new AnaDisciple());

        assertThat(gqs.hasProtectionFromSource(gd, host, greenSource)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasProtectionFromSource(gd, host, greenSource)).isFalse();
    }

    private Permanent castShield(Permanent host) {
        harness.setHand(player1, List.of(new ShieldOfDutyAndReason()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getClass() == ShieldOfDutyAndReason.class)
                .findFirst()
                .orElseThrow();
    }
}
