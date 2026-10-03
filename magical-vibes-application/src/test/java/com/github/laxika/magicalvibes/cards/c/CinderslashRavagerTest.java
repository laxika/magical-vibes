package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PredationSteward;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CinderslashRavager.class, PredationSteward.class, PropheticPrism.class})
class CinderslashRavagerTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each oil-counter permanent you control")
    void costsLessForEachOilCounterPermanentYouControl() {
        addOilPermanent(player1);
        addOilPermanent(player1);
        harness.castFromHand(player1, new CinderslashRavager(), "{2}{R}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cinderslash Ravager");
    }

    @Test
    @DisplayName("Oil counters on opponents' permanents do not reduce its cost")
    void opponentOilCountersDoNotReduceCost() {
        addOilPermanent(player2);
        harness.setHand(player1, List.of(new CinderslashRavager()));
        addManaForReducedCost();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB deals 1 damage only to creatures opponents control")
    void etbDamagesOnlyOpponentsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PredationSteward());
        harness.castFromHand(player1, new CinderslashRavager(), "{4}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple oil counters on one permanent provide only one reduction")
    void countsPermanentsRatherThanOilCounters() {
        addOilPermanent(player1).setCounterCount(CounterType.OIL, 4);
        harness.setHand(player1, List.of(new CinderslashRavager()));
        addManaForReducedCost();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Cinderslash Ravager");
    }

    @Test
    @DisplayName("Oil counters on noncreature permanents reduce its cost")
    void noncreatureOilPermanentsReduceCost() {
        for (int i = 0; i < 2; i++) {
            Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
            artifact.setCounterCount(CounterType.OIL, 1);
        }

        harness.castFromHand(player1, new CinderslashRavager(), "{2}{R}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cinderslash Ravager");
    }

    @Test
    @DisplayName("Permanents without oil counters do not reduce its cost")
    void otherCounterTypesDoNotReduceCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new CinderslashRavager()));
        addManaForReducedCost();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reduction can remove all generic mana but still requires red and green")
    void excessReductionDoesNotRemoveColoredCosts() {
        for (int i = 0; i < 6; i++) {
            addOilPermanent(player1);
        }
        harness.setHand(player1, List.of(new CinderslashRavager()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Cinderslash Ravager");
    }

    @Test
    @DisplayName("ETB damages every opposing creature, kills one-toughness creatures, and spares artifacts and players")
    void etbDamagesAllOpposingCreaturesAndSparesOtherObjects() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new PredationSteward());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PredationSteward());
        Permanent fragile = harness.addToBattlefieldAndReturn(player2, new PredationSteward());
        fragile.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new CinderslashRavager(), "{4}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second, artifact).doesNotContain(fragile);
        harness.assertInGraveyard(player2, "Predation Steward");
        assertThat(artifact.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB still deals damage after Ravager leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new PredationSteward());
        harness.castFromHand(player1, new CinderslashRavager(), "{4}{R}{G}");
        harness.passBothPriorities();
        Permanent ravager = gd.playerBattlefields.get(player1.getId()).getFirst();
        ravager.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 5);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Cinderslash Ravager");

        harness.passBothPriorities();

        assertThat(opponent.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Vigilance allows Ravager to attack without tapping")
    void vigilanceKeepsRavagerUntappedWhenAttacking() {
        Permanent ravager = addCreatureReady(player1, new CinderslashRavager());

        declareAttackers(List.of(0));

        assertThat(ravager.isTapped()).isFalse();
    }
    private Permanent addOilPermanent(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new PredationSteward());
        permanent.setCounterCount(CounterType.OIL, 1);
        return permanent;
    }

    private void addManaForReducedCost() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
