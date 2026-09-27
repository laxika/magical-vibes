package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlackManaBattery;
import com.github.laxika.magicalvibes.cards.c.CarrionAnts;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AyeshaTanaka.class, BlackManaBattery.class, CarrionAnts.class})
class AyeshaTanakaTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an artifact's activated ability when its controller cannot pay {W}")
    void countersArtifactAbilityWhenControllerCannotPay() {
        Permanent ayesha = addReadyAyesha();
        Permanent battery = addReadyBattery();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, battery.getCard().getId());
        harness.passBothPriorities();

        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(ayesha.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The artifact ability resolves when its controller pays {W}")
    void artifactAbilityResolvesWhenControllerPaysWhite() {
        addReadyAyesha();
        Permanent battery = addReadyBattery();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, battery.getCard().getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        harness.passBothPriorities();
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an activated ability from a nonartifact source")
    void cannotTargetNonartifactAbility() {
        Permanent ayesha = addReadyAyesha();
        Permanent carrionAnts = addCreatureReady(player2, new CarrionAnts());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, carrionAnts.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ayesha.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an artifact mana ability")
    void cannotTargetArtifactManaAbility() {
        Permanent ayesha = addReadyAyesha();
        Permanent battery = addReadyBattery();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, battery.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ayesha.isTapped()).isFalse();
    }

    private Permanent addReadyAyesha() {
        Permanent ayesha = harness.addToBattlefieldAndReturn(player1, new AyeshaTanaka());
        ayesha.setSummoningSick(false);
        return ayesha;
    }

    private Permanent addReadyBattery() {
        Permanent battery = harness.addToBattlefieldAndReturn(player2, new BlackManaBattery());
        battery.setSummoningSick(false);
        return battery;
    }
}
