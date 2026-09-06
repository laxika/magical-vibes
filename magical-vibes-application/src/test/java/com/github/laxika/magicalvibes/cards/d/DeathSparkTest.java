package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AgentOfStromgald;
import com.github.laxika.magicalvibes.cards.a.ArcaneDenial;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathSpark.class, AgentOfStromgald.class, ArcaneDenial.class})
class DeathSparkTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a player")
    void dealsOneDamageToPlayer() {
        harness.setHand(player1, List.of(new DeathSpark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to a creature, killing a 1-toughness creature")
    void killsSmallCreature() {
        harness.setHand(player1, List.of(new DeathSpark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player2, new AgentOfStromgald());
        UUID victim = harness.getPermanentId(player2, "Agent of Stromgald");

        harness.castInstant(player1, 0, victim);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Agent of Stromgald");
    }

    @Test
    @DisplayName("Upkeep trigger offers the {1} payment when a creature card is directly above it")
    void triggersWithCreatureDirectlyAbove() {
        harness.setGraveyard(player1, List.of(new DeathSpark(), new AgentOfStromgald()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{1}");
    }

    @Test
    @DisplayName("Paying {1} returns it from the graveyard to its owner's hand")
    void payingReturnsToHand() {
        DeathSpark spark = new DeathSpark();
        harness.setGraveyard(player1, List.of(spark, new AgentOfStromgald()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spark.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spark.getId()));
    }

    @Test
    @DisplayName("Declining keeps it in the graveyard")
    void decliningKeepsInGraveyard() {
        DeathSpark spark = new DeathSpark();
        harness.setGraveyard(player1, List.of(spark, new AgentOfStromgald()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spark.getId()));
    }

    @Test
    @DisplayName("Does not trigger when the card directly above is not a creature")
    void doesNotTriggerWithNoncreatureDirectlyAbove() {
        harness.setGraveyard(player1, List.of(new DeathSpark(), new ArcaneDenial(), new AgentOfStromgald()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when it is the top card of the graveyard")
    void doesNotTriggerWhenOnTop() {
        harness.setGraveyard(player1, List.of(new AgentOfStromgald(), new DeathSpark()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Does not return it if the condition is false when the ability resolves")
    void doesNotReturnWhenConditionFailsBeforeResolution() {
        DeathSpark spark = new DeathSpark();
        harness.setGraveyard(player1, List.of(spark, new AgentOfStromgald()));

        advanceToUpkeep(player1);
        harness.setGraveyard(player1, List.of(spark));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Death Spark");
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.setGraveyard(player1, List.of(new DeathSpark(), new AgentOfStromgald()));

        advanceToUpkeep(player2);

        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
