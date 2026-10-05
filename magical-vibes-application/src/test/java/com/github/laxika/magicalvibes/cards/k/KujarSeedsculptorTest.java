package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({KujarSeedsculptor.class, AirElemental.class, GrizzlyBears.class})
class KujarSeedsculptorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a creature you control")
    void etbPutsCounterOnOwnCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player1, List.of(new KujarSeedsculptor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, elemental.getId());
        resolveAllTriggers();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elemental.getEffectivePower()).isEqualTo(5);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new KujarSeedsculptor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Can target itself after entering an otherwise empty battlefield")
    void canTargetItself() {
        harness.setHand(player1, List.of(new KujarSeedsculptor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent sculptor = findPermanent(player1, "Kujar Seedsculptor");
        harness.handlePermanentChosen(player1, sculptor.getId());
        resolveAllTriggers();

        assertThat(sculptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sculptor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sculptor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not put a counter elsewhere when its target leaves")
    void targetLeavingMakesAbilityDoNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KujarSeedsculptor());
        harness.setHand(player1, List.of(new KujarSeedsculptor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Kujar Seedsculptor")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The target must still be controlled by the ability's controller on resolution")
    void targetChangingControllerMakesAbilityDoNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KujarSeedsculptor());
        harness.setHand(player1, List.of(new KujarSeedsculptor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter is placed even if the source leaves before the ability resolves")
    void abilityResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KujarSeedsculptor());
        harness.setHand(player1, List.of(new KujarSeedsculptor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent source = findPermanents(player1, "Kujar Seedsculptor").stream()
                .filter(permanent -> permanent != target).findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
