package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DeathcultRogue;
import com.github.laxika.magicalvibes.cards.d.DeadeyeDuelist;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JaceReawakened;
import com.github.laxika.magicalvibes.cards.o.OutlawMedic;
import com.github.laxika.magicalvibes.cards.v.VoraciousVarmint;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaughtInTheCrossfire.class, DeathcultRogue.class, HillGiant.class,
        DeadeyeDuelist.class, JaceReawakened.class, OutlawMedic.class, VoraciousVarmint.class})
class CaughtInTheCrossfireTest extends BaseCardTest {

    @Test
    @DisplayName("The outlaw mode damages outlaw creatures on both battlefields only")
    void damagesOutlawCreaturesOnly() {
        addCreatureReady(player1, new DeathcultRogue());
        addCreatureReady(player2, new DeathcultRogue());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new HillGiant());

        cast(new int[]{0}, 3);

        harness.assertNotOnBattlefield(player1, "Deathcult Rogue");
        harness.assertNotOnBattlefield(player2, "Deathcult Rogue");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The non-outlaw mode damages non-outlaw creatures only")
    void damagesNonOutlawCreaturesOnly() {
        addCreatureReady(player1, new DeathcultRogue());
        addCreatureReady(player2, new DeathcultRogue());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new HillGiant());

        cast(new int[]{1}, 3);

        harness.assertOnBattlefield(player1, "Deathcult Rogue");
        harness.assertOnBattlefield(player2, "Deathcult Rogue");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(findPermanent(player1, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Spree resolves both modes and charges both additional costs")
    void resolvesBothModes() {
        addCreatureReady(player1, new DeathcultRogue());
        addCreatureReady(player2, new DeathcultRogue());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new HillGiant());

        cast(new int[]{0, 1}, 4);

        harness.assertNotOnBattlefield(player1, "Deathcult Rogue");
        harness.assertNotOnBattlefield(player2, "Deathcult Rogue");
        assertThat(findPermanent(player1, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The non-outlaw mode leaves noncreature planeswalkers untouched")
    void nonOutlawModeDoesNotDamagePlaneswalkers() {
        var ownJace = harness.addToBattlefieldAndReturn(player1, new JaceReawakened());
        var opposingJace = harness.addToBattlefieldAndReturn(player2, new JaceReawakened());
        ownJace.setCounterCount(CounterType.LOYALTY, 3);
        opposingJace.setCounterCount(CounterType.LOYALTY, 3);
        addCreatureReady(player2, new VoraciousVarmint());

        cast(new int[]{1}, 3);

        assertThat(ownJace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(opposingJace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertInGraveyard(player2, "Voracious Varmint");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Choosing both modes still leaves noncreature planeswalkers untouched")
    void bothModesDoNotDamagePlaneswalkers() {
        var jace = harness.addToBattlefieldAndReturn(player2, new JaceReawakened());
        jace.setCounterCount(CounterType.LOYALTY, 3);

        cast(new int[]{0, 1}, 4);

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Jace Reawakened");
    }

    @Test
    @DisplayName("Both modes deal two damage, not four, to surviving outlaws")
    void bothModesDamageEachOutlawOnlyOnce() {
        var assassin = addCreatureReady(player1, new DeadeyeDuelist());
        var rogue = addCreatureReady(player2, new OutlawMedic());
        addCreatureReady(player2, new VoraciousVarmint());

        cast(new int[]{0, 1}, 4);

        harness.assertOnBattlefield(player1, "Deadeye Duelist");
        harness.assertOnBattlefield(player2, "Outlaw Medic");
        assertThat(assassin.getMarkedDamage()).isEqualTo(2);
        assertThat(rogue.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Voracious Varmint");
    }

    @Test
    @DisplayName("The non-outlaw mode deals no damage to surviving outlaws")
    void nonOutlawModeLeavesOutlawsUndamaged() {
        var assassin = addCreatureReady(player1, new DeadeyeDuelist());
        var rogue = addCreatureReady(player2, new OutlawMedic());

        cast(new int[]{1}, 3);

        assertThat(assassin.getMarkedDamage()).isZero();
        assertThat(rogue.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A single mode requires its additional mana cost")
    void cannotCastSingleModeWithOnlyBaseManaCost() {
        harness.setHand(player1, List.of(new CaughtInTheCrossfire()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both modes require paying both additional mana costs")
    void cannotCastBothModesWithOnlyOneAdditionalCost() {
        harness.setHand(player1, List.of(new CaughtInTheCrossfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, int redMana) {
        harness.setHand(player1, List.of(new CaughtInTheCrossfire()));
        harness.addMana(player1, ManaColor.RED, redMana);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, List.of());
        harness.passBothPriorities();
    }

}
