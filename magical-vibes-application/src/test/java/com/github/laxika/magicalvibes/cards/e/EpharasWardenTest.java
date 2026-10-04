package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BattlewiseValor;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.cards.v.VulpineGoliath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EpharasWarden.class, NessianCourser.class, VulpineGoliath.class,
        TravelersAmulet.class, BattlewiseValor.class})
class EpharasWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target creature with power 3 or less")
    void resolvingTapsEligibleCreature() {
        addCreatureReady(player1, new EpharasWarden());
        Permanent target = addCreatureReady(player2, new NessianCourser());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps Ephara's Warden")
    void activatingTapsSelf() {
        Permanent warden = addCreatureReady(player1, new EpharasWarden());
        Permanent target = addCreatureReady(player2, new NessianCourser());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(warden.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 3")
    void cannotTargetHighPowerCreature() {
        addCreatureReady(player1, new EpharasWarden());
        Permanent goliath = addCreatureReady(player2, new VulpineGoliath());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goliath.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can tap a creature its controller controls")
    void canTapOwnCreature() {
        addCreatureReady(player1, new EpharasWarden());
        Permanent target = addCreatureReady(player1, new NessianCourser());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target itself even though paying the cost taps it")
    void canTargetItself() {
        Permanent warden = addCreatureReady(player1, new EpharasWarden());

        harness.activateAbility(player1, 0, null, warden.getId());
        harness.passBothPriorities();

        assertThat(warden.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped creature is a legal target")
    void canTargetTappedCreature() {
        Permanent warden = addCreatureReady(player1, new EpharasWarden());
        Permanent target = addCreatureReady(player2, new NessianCourser());
        target.setTapped(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(warden.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent warden = addCreatureReady(player1, new EpharasWarden());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TravelersAmulet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(warden.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a target")
    void requiresTarget() {
        Permanent warden = addCreatureReady(player1, new EpharasWarden());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(warden.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new EpharasWarden());
        warden.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new NessianCourser());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(warden.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped Warden cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        Permanent warden = addCreatureReady(player1, new EpharasWarden());
        warden.setTapped(true);
        Permanent target = addCreatureReady(player2, new NessianCourser());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Power boosts count when checking target legality at activation")
    void cannotTargetCreatureBoostedAboveThreePower() {
        Permanent warden = addCreatureReady(player1, new EpharasWarden());
        Permanent target = addCreatureReady(player2, new NessianCourser());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new BattlewiseValor()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(warden.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature boosted above power 3 in response is not tapped")
    void doesNotTapTargetThatBecomesTooPowerful() {
        Permanent warden = addCreatureReady(player1, new EpharasWarden());
        Permanent target = addCreatureReady(player2, new NessianCourser());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new BattlewiseValor()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(warden.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves even if its source leaves the battlefield")
    void resolvesAfterWardenLeaves() {
        Permanent warden = addCreatureReady(player1, new EpharasWarden());
        Permanent target = addCreatureReady(player2, new NessianCourser());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(warden);
        gd.playerGraveyards.get(player1.getId()).add(warden.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves the battlefield is not tapped")
    void doesNotTapTargetThatLeaves() {
        Permanent warden = addCreatureReady(player1, new EpharasWarden());
        Permanent target = addCreatureReady(player2, new NessianCourser());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(warden.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
