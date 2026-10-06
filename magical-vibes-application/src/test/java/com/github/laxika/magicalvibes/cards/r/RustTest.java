package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArenaOfTheAncients;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rust.class, GreenManaBattery.class, SpinalVillain.class, ZephyrFalcon.class,
        ChainLightning.class, ArenaOfTheAncients.class, Boomerang.class})
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

        harness.castAndResolveInstant(player1, 0, battery.getId());

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

    @Test
    @DisplayName("Cannot target a triggered ability from an artifact source")
    void cannotCounterArtifactTriggeredAbility() {
        harness.setHand(player1, List.of(new Rust()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ArenaOfTheAncients()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        UUID abilityId = gd.stack.getFirst().getTargetableId();
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, abilityId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can counter your own artifact ability without refunding its costs")
    void countersOwnAbilityWithoutRefundingCosts() {
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new GreenManaBattery());
        harness.setHand(player1, List.of(new Rust()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        UUID abilityId = gd.stack.getFirst().getTargetableId();

        harness.castAndResolveInstant(player1, 0, abilityId);

        assertThat(gd.stack).isEmpty();
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertOnBattlefield(player1, "Green Mana Battery");
        harness.assertNotInGraveyard(player1, "Green Mana Battery");
        harness.assertInGraveyard(player1, "Rust");
    }

    @Test
    @DisplayName("Still counters the ability when its artifact source leaves before Rust resolves")
    void countersAbilityAfterSourceLeaves() {
        Permanent battery = harness.addToBattlefieldAndReturn(player2, new GreenManaBattery());
        harness.setHand(player1, List.of(new Rust()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        UUID abilityId = gd.stack.getFirst().getTargetableId();
        harness.passPriority(player2);
        harness.castInstant(player1, 0, abilityId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, battery.getId());
        harness.assertInHand(player2, "Green Mana Battery");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Green Mana Battery");
        harness.assertNotInGraveyard(player2, "Green Mana Battery");
        harness.assertInGraveyard(player1, "Rust");
    }
}
