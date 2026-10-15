package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WeldingJar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousGigantotherium.class, GrizzlyBears.class, Ornithopter.class, WeldingJar.class})
class RavenousGigantotheriumTest extends BaseCardTest {

    @Test
    @DisplayName("Devour 3 increases the ETB damage total to the creature's power")
    void devourPowerAndDividedDamage() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGigantotherium();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleListChoice(player1, "3");
        harness.handleListChoice(player1, "3");
        harness.passBothPriorities();

        Permanent gigantotherium = findPermanent(player1, "Ravenous Gigantotherium");
        assertThat(gigantotherium.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gigantotherium.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ETB can target creatures but not noncreature permanents")
    void etbRejectsNoncreatureTarget() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new WeldingJar());

        castGigantotherium();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGigantotherium() {
        harness.castFromHand(player1, new RavenousGigantotherium(), "{5}{G}{G}");
    }

    @Test
    @DisplayName("Choosing no targets deals no damage in either direction")
    void zeroTargetsDealsNoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGigantotherium();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(findPermanent(player1, "Ravenous Gigantotherium").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage is divided before resolution and does not change when power changes")
    void divisionRemainsFixedAfterPowerChanges() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castGigantotherium();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleListChoice(player1, "3");

        Permanent gigantotherium = findPermanent(player1, "Ravenous Gigantotherium");
        gigantotherium.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gigantotherium.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Devour may be declined and a lethally damaged target still deals damage back")
    void declineDevourAndReceiveReturnDamage() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGigantotherium();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleListChoice(player1, "3");
        harness.passBothPriorities();

        Permanent gigantotherium = findPermanent(player1, "Ravenous Gigantotherium");
        assertThat(gigantotherium.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gigantotherium.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An illegal target receives no damage and deals none back without redistributing its share")
    void illegalTargetDoesNotRedistributeDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castGigantotherium();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "2");
        first.setCounterCount(CounterType.HEXPROOF, 1);
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Ravenous Gigantotherium");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);
    }

    @Test
    @DisplayName("Power above 99 permits choosing a hundred targets")
    void canChooseOneHundredTargets() {
        List<UUID> fodder = new ArrayList<>();
        for (int i = 0; i < 33; i++) {
            fodder.add(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId());
        }
        List<UUID> targets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new Ornithopter()).getId());
        }

        castGigantotherium();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, fodder);
        harness.passBothPriorities();
        for (UUID target : targets) {
            harness.handlePermanentChosen(player1, target);
        }
        harness.handlePermanentChosen(player1, player1.getId());
        for (int i = 0; i < 99; i++) {
            harness.handleListChoice(player1, "1");
        }
        harness.handleListChoice(player1, "3");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(99);
        assertThat(findPermanent(player1, "Ravenous Gigantotherium").getMarkedDamage()).isZero();
    }
}
