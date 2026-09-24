package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AkkiRaider;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThreadsOfDisloyalty.class, AkkiRaider.class, GnarledMass.class,
        GodsEyeGateToTheReikai.class})
class ThreadsOfDisloyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving steals a creature with mana value 2 or less")
    void stealsCheapCreature() {
        Permanent creature = addCreatureReady(player2, new AkkiRaider());

        harness.setHand(player1, List.of(new ThreadsOfDisloyalty()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
    }

    @Test
    @DisplayName("Cannot enchant a creature with mana value 3 or more")
    void cannotTargetExpensiveCreature() {
        Permanent creature = addCreatureReady(player2, new GnarledMass());

        harness.setHand(player1, List.of(new ThreadsOfDisloyalty()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
    }

    @Test
    @DisplayName("Can enchant a face-down creature because its mana value is zero")
    void canTargetFaceDownCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        creature.setFaceDownAsCloaked();

        harness.setHand(player1, List.of(new ThreadsOfDisloyalty()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GodsEyeGateToTheReikai());

        harness.setHand(player1, List.of(new ThreadsOfDisloyalty()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stolenCreatures).doesNotContainKey(land.getId());
    }
}
