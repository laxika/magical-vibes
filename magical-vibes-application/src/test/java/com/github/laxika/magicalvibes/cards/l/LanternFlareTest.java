package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DaybreakCombatants;
import com.github.laxika.magicalvibes.cards.c.ChandraDressedToKill;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
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

@CardUsed({LanternFlare.class, DaybreakCombatants.class, TravelingMinister.class, ChandraDressedToKill.class})
class LanternFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast deals damage and gains life equal to creatures you control")
    void normalCastUsesCreatureCount() {
        harness.setLife(player1, 10);
        addCreatureReady(player1, new DaybreakCombatants());
        addCreatureReady(player1, new DaybreakCombatants());
        Permanent target = addCreatureReady(player2, new DaybreakCombatants());
        harness.setHand(player1, List.of(new LanternFlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player1, 12);
        harness.assertNotOnBattlefield(player2, "Daybreak Combatants");
        harness.assertInGraveyard(player2, "Daybreak Combatants");
    }

    @Test
    @DisplayName("Cleave cast uses the chosen X value")
    void cleaveCastUsesChosenXValue() {
        harness.setLife(player1, 10);
        addCreatureReady(player1, new DaybreakCombatants());
        Permanent target = addCreatureReady(player2, new DaybreakCombatants());
        harness.setHand(player1, List.of(new LanternFlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCardWithAlternateCost(gd, player1, 0, 3, target.getId(), null, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertNotOnBattlefield(player2, "Daybreak Combatants");
        harness.assertInGraveyard(player2, "Daybreak Combatants");
    }

    @Test
    void normalCastWithNoCreaturesDealsNoDamageAndGainsNoLife() {
        harness.setLife(player1, 10);
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        harness.setHand(player1, List.of(new LanternFlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player1, 10);
        harness.assertOnBattlefield(player2, "Traveling Minister");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void normalCastCountsOwnLethallyDamagedTargetForLifeGain() {
        harness.setLife(player1, 10);
        Permanent target = addCreatureReady(player1, new TravelingMinister());
        harness.setHand(player1, List.of(new LanternFlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player1, 11);
        harness.assertNotOnBattlefield(player1, "Traveling Minister");
        harness.assertInGraveyard(player1, "Traveling Minister");
    }

    @Test
    void normalCastCountsCreaturesAtResolution() {
        harness.setLife(player1, 10);
        Permanent ownCreature = addCreatureReady(player1, new TravelingMinister());
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        harness.setHand(player1, List.of(new LanternFlare()));
        harness.setHand(player2, List.of(new LanternFlare()));
        for (var player : List.of(player1, player2)) {
            harness.addMana(player, ManaColor.WHITE, 1);
            harness.addMana(player, ManaColor.COLORLESS, 1);
        }

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, ownCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Traveling Minister");
        harness.assertOnBattlefield(player2, "Traveling Minister");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void illegalTargetAtResolutionPreventsLifeGain() {
        harness.setLife(player1, 10);
        addCreatureReady(player1, new TravelingMinister());
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        harness.setHand(player1, List.of(new LanternFlare()));
        harness.setHand(player2, List.of(new LanternFlare()));
        for (var player : List.of(player1, player2)) {
            harness.addMana(player, ManaColor.WHITE, 1);
            harness.addMana(player, ManaColor.COLORLESS, 1);
        }

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player2, "Traveling Minister");
        harness.assertInGraveyard(player1, "Lantern Flare");
    }

    @Test
    void cleaveAllowsZeroDespiteControllingCreatures() {
        harness.setLife(player1, 10);
        addCreatureReady(player1, new TravelingMinister());
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        harness.setHand(player1, List.of(new LanternFlare()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertOnBattlefield(player2, "Traveling Minister");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void normalCastDamagesPlaneswalkerAndGainsLife() {
        harness.setLife(player1, 10);
        addCreatureReady(player1, new TravelingMinister());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraDressedToKill());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new LanternFlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player1, 11);
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void cleaveDamagesPlaneswalkerWithoutControllingCreatures() {
        harness.setLife(player1, 10);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraDressedToKill());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new LanternFlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCardWithAlternateCost(gd, player1, 0, 3, target.getId(), null, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertNotOnBattlefield(player2, "Chandra, Dressed to Kill");
        harness.assertInGraveyard(player2, "Chandra, Dressed to Kill");
    }

    @Test
    void normalCastCannotTargetPlayer() {
        harness.setHand(player1, List.of(new LanternFlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cleaveCannotTargetPlayer() {
        harness.setHand(player1, List.of(new LanternFlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(
                gd, player1, 0, 3, player2.getId(), null, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
