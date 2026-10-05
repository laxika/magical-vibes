package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecraDisciple.class, GarrukWildspeaker.class, GrizzlyBears.class, Plains.class, Shock.class})
class NecraDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Green ability adds one mana of the chosen color")
    void greenAbilityAddsAnyColorMana() {
        Permanent disciple = addReadyDisciple(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(disciple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("White ability prevents the next damage to the targeted creature")
    void whiteAbilityPreventsDamageToTargetCreature() {
        addReadyDisciple(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("White ability only protects its chosen target")
    void whiteAbilityOnlyProtectsChosenTarget() {
        addReadyDisciple(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("White ability can target a planeswalker")
    void whiteAbilityCanTargetPlaneswalker() {
        addReadyDisciple(player1);
        Permanent garruk = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        garruk.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, garruk.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, garruk.getId());

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("White ability cannot target a land")
    void cannotTargetLand() {
        addReadyDisciple(player1);
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Green ability produces exactly one mana immediately and consumes its green cost")
    void greenAbilityProducesEachColorWithoutUsingTheStack(ManaColor color) {
        Permanent disciple = addReadyDisciple(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        if (color != ManaColor.GREEN) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        }
        assertThat(disciple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("White ability uses the stack and its player shield is consumed by the first damage")
    void playerShieldPreventsOnlyOneDamageInTotal() {
        Permanent disciple = addReadyDisciple(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(disciple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 19);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("White ability still resolves after the Disciple is destroyed in response")
    void preventionResolvesAfterSourceLeavesBattlefield() {
        Permanent disciple = addReadyDisciple(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, player2.getId());

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, disciple.getId());
        harness.assertInGraveyard(player1, "Necra Disciple");
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Two Disciples can create cumulative prevention shields on the same player")
    void preventionShieldsAccumulate() {
        addReadyDisciple(player1);
        addReadyDisciple(player1);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertLife(player1, 20);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An unused prevention shield expires at the end of the turn")
    void unusedPlayerShieldExpires() {
        addReadyDisciple(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both tap abilities are unavailable while summoning sick")
    void cannotActivateWhileSummoningSick(int abilityIndex) {
        harness.addToBattlefield(player1, new NecraDisciple());
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.GREEN : ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null,
                abilityIndex == 0 ? null : player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both abilities require an untapped Disciple")
    void cannotActivateWhileTapped(int abilityIndex) {
        Permanent disciple = addReadyDisciple(player1);
        disciple.setTapped(true);
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.GREEN : ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null,
                abilityIndex == 0 ? null : player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both abilities require their colored mana cost")
    void cannotActivateWithoutColoredMana(int abilityIndex) {
        addReadyDisciple(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null,
                abilityIndex == 0 ? null : player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDisciple(Player player) {
        return addCreatureReady(player, new NecraDisciple());
    }
}
