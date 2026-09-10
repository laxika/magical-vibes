package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DuskriderFalcon;
import com.github.laxika.magicalvibes.cards.d.DwarvenBerserker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Thunderbolt.class, DuskriderFalcon.class, DwarvenBerserker.class, ChandraNalaar.class})
class ThunderboltTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Mode 0 deals 3 damage to target player")
    void mode0DamagesPlayer() {
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Thunderbolt");
    }

    @Test
    @DisplayName("Mode 0 deals 3 damage to target planeswalker")
    void mode0DamagesPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        harness.castInstant(player1, 0, 0, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode 1 deals 4 damage to a creature with flying, killing it")
    void mode1KillsFlyingCreature() {
        harness.addToBattlefield(player2, new DuskriderFalcon());
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        UUID falconId = harness.getPermanentId(player2, "Duskrider Falcon");
        harness.castModalInstant(player1, 0, 1, List.of(falconId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Duskrider Falcon");
        harness.assertInGraveyard(player2, "Duskrider Falcon");
    }

    @Test
    @DisplayName("Mode 1 deals exactly 4 damage to a large flier")
    void mode1DealsFourDamage() {
        DuskriderFalcon bigFalcon = new DuskriderFalcon();
        bigFalcon.setPower(5);
        bigFalcon.setToughness(5);
        harness.addToBattlefield(player2, bigFalcon);
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        UUID falconId = harness.getPermanentId(player2, "Duskrider Falcon");
        harness.castModalInstant(player1, 0, 1, List.of(falconId));
        harness.passBothPriorities();

        Permanent hawk = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(falconId))
                .findFirst().orElseThrow();
        assertThat(hawk.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Mode 1 cannot target a creature without flying")
    void mode1CannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player2, new DuskriderFalcon());
        harness.addToBattlefield(player2, new DwarvenBerserker());
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        UUID berserkerId = harness.getPermanentId(player2, "Dwarven Berserker");
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(berserkerId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
