package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraTheFirebrand;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Magmaquake.class, WalkingCorpse.class, WindDrake.class, ChandraTheFirebrand.class})
class MagmaquakeTest extends BaseCardTest {

    @Test
    @DisplayName("Damages planeswalkers even when they have flying")
    void damagesFlyingPlaneswalkers() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraTheFirebrand());
        chandra.setCounterCount(CounterType.LOYALTY, 3);
        chandra.getPersistentGrantedKeywords().add(Keyword.FLYING);
        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Chandra, the Firebrand");
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Marks nonlethal damage on creatures and removes loyalty from both players' planeswalkers")
    void dealsNonlethalDamageAcrossBothBattlefields() {
        Permanent corpse = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent ownChandra = harness.addToBattlefieldAndReturn(player1, new ChandraTheFirebrand());
        Permanent opposingChandra = harness.addToBattlefieldAndReturn(player2, new ChandraTheFirebrand());
        ownChandra.setCounterCount(CounterType.LOYALTY, 3);
        opposingChandra.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstantForX(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Walking Corpse");
        assertThat(corpse.getMarkedDamage()).isEqualTo(1);
        assertThat(ownChandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(opposingChandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals X damage to each creature without flying")
    void killsNonFlyingCreatures() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Does not damage creatures with flying")
    void doesNotDamageFlyers() {
        harness.addToBattlefield(player2, new WindDrake());
        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstantForX(player1, 0, 4, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Deals X damage to each planeswalker")
    void damagesPlaneswalkers() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraTheFirebrand());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstantForX(player1, 0, 6, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chandra, the Firebrand");
    }

    @Test
    @DisplayName("Deals no damage to players")
    void doesNotDamagePlayers() {
        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstantForX(player1, 0, 5, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("X=0 leaves creatures alive")
    void xZeroDealsNoDamage() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Walking Corpse");
    }
}
