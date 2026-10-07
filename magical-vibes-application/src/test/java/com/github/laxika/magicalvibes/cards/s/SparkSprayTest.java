package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SparkSpray.class, GoblinBrigand.class})
class SparkSprayTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void dealsDamageToPlayer() {
        harness.setHand(player1, List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature")
    void dealsDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        harness.setHand(player1, List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling discards Spark Spray and draws one card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new SparkSpray()));
        harness.setLibrary(player1, List.of(new GoblinBrigand()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spark Spray");
        harness.assertInHand(player1, "Goblin Brigand");
    }

    @Test
    @DisplayName("Can deal damage to its controller")
    void canTargetController() {
        harness.setHand(player1, List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Spark Spray");
    }

    @Test
    @DisplayName("Damage combines with existing damage to kill a creature")
    void damageCanBeLethal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        harness.setHand(player1, List.of(new SparkSpray(), new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertOnBattlefield(player2, "Goblin Brigand");
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Goblin Brigand");
        harness.assertInGraveyard(player2, "Goblin Brigand");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not deal damage when its only target leaves before resolution")
    void targetLeavingBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        harness.setHand(player1, List.of(new SparkSpray(), new SparkSpray(), new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInGraveyard(player2, "Goblin Brigand");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cycling discards as a cost and draws only on resolution without dealing damage")
    void cyclingUsesTheStackWithoutDealingDamage() {
        harness.setHand(player1, List.of(new SparkSpray()));
        harness.setLibrary(player1, List.of(new GoblinBrigand(), new GoblinBrigand()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Spark Spray");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Goblin Brigand");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cycling cannot be paid with blue mana instead of red")
    void cyclingRequiresRedMana() {
        harness.setHand(player1, List.of(new SparkSpray()));
        harness.setLibrary(player1, List.of(new GoblinBrigand()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Spark Spray");
        harness.assertNotInGraveyard(player1, "Spark Spray");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
