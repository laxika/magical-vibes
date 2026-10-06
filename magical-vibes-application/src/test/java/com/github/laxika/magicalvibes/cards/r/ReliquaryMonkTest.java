package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.cards.s.Sanctimony;
import com.github.laxika.magicalvibes.cards.t.ThranDynamo;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReliquaryMonk.class, MetathranSoldier.class, RecklessAbandon.class, Sanctimony.class,
        ThranDynamo.class, Rescue.class})
class ReliquaryMonkTest extends BaseCardTest {

    @Test
    @DisplayName("When Reliquary Monk dies, it destroys target artifact")
    void diesDestroysTargetArtifact() {
        UUID monkId = harness.addToBattlefieldAndReturn(player1, new ReliquaryMonk()).getId();
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new ThranDynamo()).getId();

        castRecklessAbandonAt(monkId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifactId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Reliquary Monk");
        harness.assertNotOnBattlefield(player2, "Thran Dynamo");
        harness.assertInGraveyard(player2, "Thran Dynamo");
    }

    @Test
    @DisplayName("When Reliquary Monk dies, it destroys target enchantment")
    void diesDestroysTargetEnchantment() {
        UUID monkId = harness.addToBattlefieldAndReturn(player1, new ReliquaryMonk()).getId();
        UUID enchantmentId = harness.addToBattlefieldAndReturn(player2, new Sanctimony()).getId();

        castRecklessAbandonAt(monkId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, enchantmentId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Reliquary Monk");
        harness.assertNotOnBattlefield(player2, "Sanctimony");
        harness.assertInGraveyard(player2, "Sanctimony");
    }

    @Test
    @DisplayName("Death trigger only offers artifacts and enchantments as valid targets")
    void targetFilterOnlyArtifactsAndEnchantments() {
        UUID monkId = harness.addToBattlefieldAndReturn(player1, new ReliquaryMonk()).getId();
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new ThranDynamo()).getId();
        UUID enchantmentId = harness.addToBattlefieldAndReturn(player2, new Sanctimony()).getId();
        harness.addToBattlefield(player2, new MetathranSoldier());

        castRecklessAbandonAt(monkId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(artifactId, enchantmentId);
    }

    @Test
    @DisplayName("When Reliquary Monk dies, it can destroy an artifact its controller controls")
    void diesDestroysOwnArtifact() {
        UUID monkId = harness.addToBattlefieldAndReturn(player1, new ReliquaryMonk()).getId();
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, new ThranDynamo()).getId();

        castRecklessAbandonAt(monkId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifactId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thran Dynamo");
    }

    @Test
    @DisplayName("When Reliquary Monk dies without a legal target, its death trigger is skipped")
    void deathTriggerIsSkippedWithoutLegalTarget() {
        UUID monkId = harness.addToBattlefieldAndReturn(player1, new ReliquaryMonk()).getId();

        castRecklessAbandonAt(monkId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Reliquary Monk");
    }

    @Test
    @DisplayName("Sacrificing Reliquary Monk triggers destruction before the sacrifice spell resolves")
    void sacrificeTriggersDestruction() {
        UUID monkId = harness.addToBattlefieldAndReturn(player1, new ReliquaryMonk()).getId();
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new ThranDynamo()).getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), monkId);

        harness.assertInGraveyard(player1, "Reliquary Monk");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifactId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Thran Dynamo");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Returning Reliquary Monk to hand does not trigger destruction")
    void returningToHandDoesNotTrigger() {
        UUID monkId = harness.addToBattlefieldAndReturn(player1, new ReliquaryMonk()).getId();
        harness.addToBattlefield(player2, new ThranDynamo());
        harness.setHand(player1, List.of(new Rescue()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, monkId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Reliquary Monk");
        harness.assertNotInGraveyard(player1, "Reliquary Monk");
        harness.assertOnBattlefield(player2, "Thran Dynamo");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Death trigger does not destroy another permanent when its target leaves")
    void targetLeavesBeforeResolution() {
        UUID monkId = harness.addToBattlefieldAndReturn(player1, new ReliquaryMonk()).getId();
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new ThranDynamo()).getId();
        harness.addToBattlefield(player2, new Sanctimony());

        castRecklessAbandonAt(monkId);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifactId);
        harness.setHand(player2, List.of(new Rescue()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, artifactId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Thran Dynamo");
        harness.assertNotInGraveyard(player2, "Thran Dynamo");
        harness.assertOnBattlefield(player2, "Sanctimony");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castRecklessAbandonAt(UUID targetId) {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new MetathranSoldier()).getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player1, 0, targetId, sacrificeId);
    }
}
