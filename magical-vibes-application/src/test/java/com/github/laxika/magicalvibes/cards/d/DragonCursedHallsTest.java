package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonCursedHalls.class, GrizzlyBears.class})
class DragonCursedHallsTest extends BaseCardTest {

    @Test
    @DisplayName("Taps to add colorless mana")
    void tapsForColorlessMana() {
        Permanent halls = harness.addToBattlefieldAndReturn(player1, new DragonCursedHalls());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(halls.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Grants a creature a combat-damage Treasure trigger until end of turn")
    void grantsTreasureTrigger() {
        harness.addToBattlefield(player1, new DragonCursedHalls());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The activated ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent halls = harness.addToBattlefieldAndReturn(player1, new DragonCursedHalls());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, halls.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(halls.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opposing creature creates Treasure for its own controller")
    void opposingCreatureCreatesTreasure() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new DragonCursedHalls());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Two grants create two Treasures from one combat damage event")
    void repeatedGrantsTriggerSeparately() {
        harness.addToBattlefield(player1, new DragonCursedHalls());
        harness.addToBattlefield(player1, new DragonCursedHalls());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, creature.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Combat damage to a blocker does not create Treasure")
    void blockedCreatureDoesNotCreateTreasure() {
        harness.addToBattlefield(player1, new DragonCursedHalls());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The granted ability expires during cleanup")
    void grantedAbilityExpires() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new DragonCursedHalls());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }
}
