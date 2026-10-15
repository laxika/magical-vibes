package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.t.TheWeatherseedTreaty;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Defiler of Vigor")
@CardUsed({DefilerOfVigor.class, GrizzlyBears.class, LlanowarElves.class, SavannahLions.class,
        GiantGrowth.class, TheWeatherseedTreaty.class})
class DefilerOfVigorTest extends BaseCardTest {

    @Test
    @DisplayName("paying 2 life reduces a green permanent spell by {G} and puts counters on each creature you control")
    void paysLifeForGreenPermanentSpell() {
        Permanent defiler = addCreatureReady(player1, new DefilerOfVigor());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.setLife(player1, 20);

        harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(defiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("declining the life payment pays the full mana cost and still puts counters on each creature you control")
    void paysReducedManaForGreenPermanentSpell() {
        Permanent defiler = addCreatureReady(player1, new DefilerOfVigor());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(defiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("non-green permanent and green nonpermanent spells do not trigger")
    void ignoresNonMatchingSpells() {
        Permanent defiler = addCreatureReady(player1, new DefilerOfVigor());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SavannahLions()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(defiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, ownCreature.getId());
        resolveAllTriggers();

        assertThat(defiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("two Defilers do not reduce the mana cost when no life is paid")
    void multipleDefilersDoNotReduceCostWithoutLifePayment() {
        Permanent first = addCreatureReady(player1, new DefilerOfVigor());
        Permanent second = addCreatureReady(player1, new DefilerOfVigor());
        harness.setHand(player1, List.of(new DefilerOfVigor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("paying only 2 life with two Defilers reduces the cost by only one green mana")
    void multipleDefilersRequireSeparateLifePaymentsForEachReduction() {
        addCreatureReady(player1, new DefilerOfVigor());
        addCreatureReady(player1, new DefilerOfVigor());
        harness.setHand(player1, List.of(new DefilerOfVigor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Defiler does not reduce its own cost or trigger for its own casting")
    void doesNotApplyBeforeEnteringBattlefield() {
        harness.setHand(player1, List.of(new DefilerOfVigor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Defiler of Vigor");
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("the creature spell enters after the cast trigger and receives no counter")
    void castTriggerResolvesBeforeCreatureEnters() {
        Permanent defiler = addCreatureReady(player1, new DefilerOfVigor());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(defiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof LlanowarElves)
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("a green enchantment gets the reduction and triggers without reducing generic mana")
    void appliesToGreenNoncreaturePermanentSpell() {
        Permanent defiler = addCreatureReady(player1, new DefilerOfVigor());
        harness.setHand(player1, List.of(new TheWeatherseedTreaty()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(defiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "The Weatherseed Treaty");
    }

    @Test
    @DisplayName("an opponent's green permanent spell gets no reduction and does not trigger")
    void doesNotApplyToOpponentsSpells() {
        Permanent defiler = addCreatureReady(player1, new DefilerOfVigor());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DefilerOfVigor()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Defiler of Vigor");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(defiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }
}
