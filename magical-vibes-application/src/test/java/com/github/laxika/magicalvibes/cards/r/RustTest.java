package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.g.GreenManaBattery;
import com.github.laxika.magicalvibes.cards.s.SpinalVillain;
import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rust.class, GreenManaBattery.class, SpinalVillain.class, ZephyrFalcon.class, ChainLightning.class})
class RustTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an activated ability from an artifact source")
    void countersArtifactActivatedAbility() {
        GreenManaBattery battery = new GreenManaBattery();
        Permanent batteryPermanent = harness.addToBattlefieldAndReturn(player2, battery);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.setHand(player1, List.of(new Rust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, battery.getId());
        harness.passBothPriorities();

        assertThat(batteryPermanent.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a mana ability")
    void cannotCounterManaAbility() {
        GreenManaBattery battery = new GreenManaBattery();
        harness.addToBattlefield(player2, battery);

        harness.setHand(player1, List.of(new Rust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passPriority(player2);

        assertThat(harness.getGameData().playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, battery.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability from a non-artifact source")
    void cannotCounterNonArtifactAbility() {
        SpinalVillain villain = new SpinalVillain();
        Permanent villainPermanent = harness.addToBattlefieldAndReturn(player2, villain);
        villainPermanent.setSummoningSick(false);
        harness.addToBattlefield(player2, new ZephyrFalcon());

        harness.setHand(player1, List.of(new Rust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, harness.getPermanentId(player2, "Zephyr Falcon"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, villain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotCounterSpell() {
        ChainLightning chainLightning = new ChainLightning();
        harness.setHand(player2, List.of(chainLightning));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new Rust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, chainLightning.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
