package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DragonsPrey;
import com.github.laxika.magicalvibes.cards.e.ElspethStormSlayer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UreniTheSongUnending.class, Forest.class, GarrukWildspeaker.class, GrizzlyBears.class, DragonsPrey.class, ElspethStormSlayer.class})
class UreniTheSongUnendingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals the land-count damage among opponent creatures and planeswalkers")
    void etbDealsLandCountDamageAmongTargets() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 1, planeswalker.getId(), 2);

        castUreni(List.of(creature.getId(), planeswalker.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB cannot target a creature its controller controls")
    void etbRejectsOwnCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareToCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker an opponent controls");
    }

    @Test
    @DisplayName("ETB damage stays fixed when another land enters after the trigger is stacked")
    void etbDamageDoesNotIncreaseWithLaterLand() {
        harness.addToBattlefield(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 1);
        prepareToCast();
        harness.castCreature(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB damage stays fixed when a land leaves after the trigger is stacked")
    void etbDamageDoesNotDecreaseWithLaterLandLoss() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 1);
        prepareToCast();
        harness.castCreature(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerHands.get(player1.getId()).add(land.getCard());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage assigned to a departed target is not reassigned to a remaining target")
    void etbKeepsOriginalDivisionWhenOneTargetLeaves() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(remaining.getId(), 1, departed.getId(), 1);
        prepareToCast();
        harness.castCreature(player1, 0, List.of(remaining.getId(), departed.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(departed);
        gd.playerHands.get(player2.getId()).add(departed.getCard());
        harness.passBothPriorities();

        assertThat(remaining.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(remaining);
    }

    @Test
    @DisplayName("ETB may choose no targets even when its controller has lands")
    void etbMayChooseNoTargets() {
        harness.addToBattlefield(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castUreni(List.of());

        harness.assertOnBattlefield(player1, "Ureni, the Song Unending");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Protection from black prevents Dragon's Prey from targeting Ureni")
    void protectionRejectsBlackSpell() {
        Permanent ureni = harness.addToBattlefieldAndReturn(player2, new UreniTheSongUnending());
        harness.setHand(player1, List.of(new DragonsPrey()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ureni.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        harness.assertOnBattlefield(player2, "Ureni, the Song Unending");
    }

    @Test
    @DisplayName("Protection from white prevents Elspeth's removal ability from targeting Ureni")
    void protectionRejectsWhiteAbility() {
        Permanent ureni = harness.addToBattlefieldAndReturn(player2, new UreniTheSongUnending());
        Permanent elspeth = harness.addToBattlefieldAndReturn(player1, new ElspethStormSlayer());
        elspeth.setCounterCount(CounterType.LOYALTY, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, ureni.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        harness.assertOnBattlefield(player2, "Ureni, the Song Unending");
    }

    private void castUreni(List<java.util.UUID> targetIds) {
        prepareToCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareToCast() {
        harness.setHand(player1, List.of(new UreniTheSongUnending()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
