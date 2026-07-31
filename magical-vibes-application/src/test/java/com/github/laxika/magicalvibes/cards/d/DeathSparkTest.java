package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeathSparkTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a player")
    void dealsOneDamageToPlayer() {
        harness.setHand(player1, List.of(new DeathSpark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to a creature, killing a 1-toughness creature")
    void killsSmallCreature() {
        harness.setHand(player1, List.of(new DeathSpark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player2, new FugitiveWizard());
        UUID victim = harness.getPermanentId(player2, "Fugitive Wizard");

        harness.castInstant(player1, 0, victim);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Upkeep trigger offers the {1} payment when a creature card is directly above it")
    void triggersWithCreatureDirectlyAbove() {
        harness.setGraveyard(player1, List.of(new DeathSpark(), new GrizzlyBears()));

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
        harness.setGraveyard(player1, List.of(spark, new GrizzlyBears()));

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
        harness.setGraveyard(player1, List.of(spark, new GrizzlyBears()));

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
        harness.setGraveyard(player1, List.of(new DeathSpark(), new Shock(), new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.setGraveyard(player1, List.of(new DeathSpark(), new GrizzlyBears()));

        advanceToUpkeep(player2);

        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
