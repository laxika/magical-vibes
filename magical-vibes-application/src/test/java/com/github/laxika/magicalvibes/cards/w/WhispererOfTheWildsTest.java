package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.b.BomatBazaarBarge;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhispererOfTheWilds.class, AvatarOfMight.class, BomatBazaarBarge.class})
class WhispererOfTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one green mana")
    void firstAbilityAddsOneGreenMana() {
        addCreatureReady(player1, new WhispererOfTheWilds());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ferocious ability adds two green mana when controlling a creature with power 4 or greater")
    void ferociousAbilityAddsTwoGreenMana() {
        addCreatureReady(player1, new WhispererOfTheWilds());
        harness.addToBattlefield(player1, new AvatarOfMight());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ferocious ability cannot activate without a creature with power 4 or greater")
    void ferociousAbilityRequiresBigCreatureYouControl() {
        addCreatureReady(player1, new WhispererOfTheWilds());
        harness.addToBattlefield(player2, new AvatarOfMight());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void ferociousUsesModifiedPowerAndIncludesWhispererItself() {
        Permanent whisperer = addCreatureReady(player1, new WhispererOfTheWilds());
        whisperer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
        assertThat(whisperer.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        whisperer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(whisperer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void uncrewedVehicleDoesNotEnableFerocious() {
        Permanent whisperer = addCreatureReady(player1, new WhispererOfTheWilds());
        harness.addToBattlefield(player1, new BomatBazaarBarge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");

        assertThat(whisperer.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void bothAbilitiesRequireAnUntappedSource(int abilityIndex) {
        Permanent whisperer = addCreatureReady(player1, new WhispererOfTheWilds());
        whisperer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        whisperer.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void bothAbilitiesAreBlockedBySummoningSickness(int abilityIndex) {
        Permanent whisperer = harness.addToBattlefieldAndReturn(player1, new WhispererOfTheWilds());
        whisperer.setSummoningSick(true);
        whisperer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(whisperer.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void firstAbilityStillAddsOnlyOneManaWithFerocious() {
        Permanent whisperer = addCreatureReady(player1, new WhispererOfTheWilds());
        whisperer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(whisperer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

}
