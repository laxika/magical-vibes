package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SteadfastGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RishadanPort.class, Forest.class, SteadfastGuard.class})
class RishadanPortTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one colorless mana")
    void manaAbilityAddsColorlessMana() {
        harness.addToBattlefield(player1, new RishadanPort());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanent(player1, "Rishadan Port").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps target land")
    void tapsTargetLand() {
        Permanent port = harness.addToBattlefieldAndReturn(player1, new RishadanPort());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(port.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Can target its controller's land")
    void tapsOwnLand() {
        Permanent port = harness.addToBattlefieldAndReturn(player1, new RishadanPort());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(port.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target an already tapped land")
    void canTargetAlreadyTappedLand() {
        Permanent port = harness.addToBattlefieldAndReturn(player1, new RishadanPort());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(port.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new RishadanPort());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SteadfastGuard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Target land can produce mana in response")
    void targetLandCanProduceManaInResponse() {
        harness.addToBattlefield(player1, new RishadanPort());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, forest.getId());

        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.tapPermanent(player2, 0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself despite tapping to pay the cost")
    void canTargetItself() {
        Permanent port = harness.addToBattlefieldAndReturn(player1, new RishadanPort());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, port.getId());
        assertThat(port.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(port.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot activate land tapping ability without mana")
    void cannotActivateWithoutMana() {
        Permanent port = harness.addToBattlefieldAndReturn(player1, new RishadanPort());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(port.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Producing mana prevents using the Port again without untapping")
    void cannotActivateAfterProducingMana() {
        harness.addToBattlefield(player1, new RishadanPort());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
