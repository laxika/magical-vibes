package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Inspirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarbleTitan.class, HillGiant.class, GrizzlyBears.class, DuskLegionDreadnought.class,
        DeepFreeze.class, GloriousAnthem.class, Inspirit.class})
class MarbleTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped creature with power 3+ does not untap while Marble Titan is out")
    void power3CreatureStaysTapped() {
        addCreatureReady(player1, new MarbleTitan());
        Permanent giant = addCreatureReady(player1, new HillGiant()); // 3/3
        giant.tap();

        advanceToUpkeep(player1);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped creature with power under 3 untaps normally")
    void power2CreatureUntaps() {
        addCreatureReady(player1, new MarbleTitan());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        bears.tap();

        advanceToUpkeep(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A noncreature permanent with power 3+ untaps normally")
    void noncreatureWithPowerThreeOrGreaterUntaps() {
        addCreatureReady(player1, new MarbleTitan());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought()); // 4/6
        vehicle.tap();

        advanceToUpkeep(player1);

        assertThat(vehicle.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Marble Titan (a 3/3) locks itself as well")
    void marbleTitanLocksItself() {
        Permanent titan = addCreatureReady(player1, new MarbleTitan()); // 3/3
        titan.tap();

        advanceToUpkeep(player1);

        assertThat(titan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Affects opponents' creatures during their untap step")
    void affectsOpponentCreatures() {
        addCreatureReady(player1, new MarbleTitan());
        Permanent opponentGiant = addCreatureReady(player2, new HillGiant()); // 3/3
        opponentGiant.tap();

        advanceToUpkeep(player2);

        assertThat(opponentGiant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Once Marble Titan leaves, power 3+ creatures untap again")
    void untapsAfterTitanLeaves() {
        Permanent titan = addCreatureReady(player1, new MarbleTitan());
        Permanent giant = addCreatureReady(player1, new HillGiant()); // 3/3
        giant.tap();

        gd.playerBattlefields.get(player1.getId()).remove(titan);

        advanceToUpkeep(player1);

        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Marble Titan that loses its abilities no longer prevents other creatures from untapping")
    void disabledMarbleTitanStopsLockingCreatures() {
        Permanent disabledTitan = addCreatureReady(player1, new MarbleTitan());
        Permanent giant = addCreatureReady(player1, new HillGiant()); // 3/3
        disabledTitan.tap();
        giant.tap();

        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, disabledTitan.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(disabledTitan.isTapped()).isFalse();
        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature boosted from power two to three stays tapped")
    void increasedEffectivePowerPreventsUntapping() {
        addCreatureReady(player1, new MarbleTitan());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());
        bears.tap();

        advanceToUpkeep(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature whose base power is reduced below three untaps")
    void reducedEffectivePowerAllowsUntapping() {
        addCreatureReady(player1, new MarbleTitan());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        giant.tap();
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An untap spell can untap a creature with power three or greater")
    void spellUntapsCreatureOutsideUntapStep() {
        addCreatureReady(player1, new MarbleTitan());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        giant.tap();
        harness.setHand(player1, List.of(new Inspirit()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, giant.getId());

        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An active Marble Titan still locks creatures when another Titan loses its abilities")
    void secondTitanContinuesPreventingUntapping() {
        Permanent disabledTitan = addCreatureReady(player1, new MarbleTitan());
        addCreatureReady(player2, new MarbleTitan());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        giant.tap();
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, disabledTitan.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(giant.isTapped()).isTrue();
    }
}
