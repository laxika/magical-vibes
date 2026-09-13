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
        ThranDynamo.class})
class ReliquaryMonkTest extends BaseCardTest {

    @Test
    @DisplayName("When Reliquary Monk dies, it destroys target artifact")
    void diesDestroysTargetArtifact() {
        harness.addToBattlefield(player1, new ReliquaryMonk());
        harness.addToBattlefield(player2, new ThranDynamo());

        UUID monkId = harness.getPermanentId(player1, "Reliquary Monk");
        UUID artifactId = harness.getPermanentId(player2, "Thran Dynamo");

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
        harness.addToBattlefield(player1, new ReliquaryMonk());
        harness.addToBattlefield(player2, new Sanctimony());

        UUID monkId = harness.getPermanentId(player1, "Reliquary Monk");
        UUID enchantmentId = harness.getPermanentId(player2, "Sanctimony");

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
        harness.addToBattlefield(player1, new ReliquaryMonk());
        harness.addToBattlefield(player2, new ThranDynamo());
        harness.addToBattlefield(player2, new Sanctimony());
        harness.addToBattlefield(player2, new MetathranSoldier());

        UUID monkId = harness.getPermanentId(player1, "Reliquary Monk");
        UUID artifactId = harness.getPermanentId(player2, "Thran Dynamo");
        UUID enchantmentId = harness.getPermanentId(player2, "Sanctimony");

        castRecklessAbandonAt(monkId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(artifactId, enchantmentId);
    }

    @Test
    @DisplayName("When Reliquary Monk dies, it can destroy an artifact its controller controls")
    void diesDestroysOwnArtifact() {
        harness.addToBattlefield(player1, new ReliquaryMonk());
        harness.addToBattlefield(player1, new ThranDynamo());

        UUID monkId = harness.getPermanentId(player1, "Reliquary Monk");
        UUID artifactId = harness.getPermanentId(player1, "Thran Dynamo");

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
        harness.addToBattlefield(player1, new ReliquaryMonk());

        UUID monkId = harness.getPermanentId(player1, "Reliquary Monk");

        castRecklessAbandonAt(monkId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Reliquary Monk");
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
