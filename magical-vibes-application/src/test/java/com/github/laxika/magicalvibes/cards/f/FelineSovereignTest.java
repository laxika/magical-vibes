package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChromeReplicator;
import com.github.laxika.magicalvibes.cards.g.GarruksUprising;
import com.github.laxika.magicalvibes.cards.p.PackLeader;
import com.github.laxika.magicalvibes.cards.s.SabertoothMauler;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FelineSovereign.class, SabertoothMauler.class, PackLeader.class,
        ChromeReplicator.class, GarruksUprising.class})
class FelineSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("Other Cats get +1/+1 and protection from Dogs")
    void otherCatsGetBoostAndProtectionFromDogs() {
        Permanent cat = addCreatureReady(player1, new SabertoothMauler());
        Permanent nonCat = addCreatureReady(player1, new PackLeader());
        int catPower = gqs.getEffectivePower(gd, cat);
        int catToughness = gqs.getEffectiveToughness(gd, cat);
        int nonCatPower = gqs.getEffectivePower(gd, nonCat);
        int nonCatToughness = gqs.getEffectiveToughness(gd, nonCat);
        Permanent dog = addCreatureReady(player2, new PackLeader());

        addCreatureReady(player1, new FelineSovereign());

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(catPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(catToughness + 1);
        assertThat(gqs.getEffectivePower(gd, nonCat)).isEqualTo(nonCatPower);
        assertThat(gqs.getEffectiveToughness(gd, nonCat)).isEqualTo(nonCatToughness);
        assertThat(gqs.hasProtectionFromSource(gd, cat, dog)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, nonCat, dog)).isFalse();
    }

    @Test
    @DisplayName("A Cat dealing combat damage presents up to one artifact or enchantment")
    void combatDamageDestroysUpToOneArtifactOrEnchantment() {
        addCreatureReady(player1, new FelineSovereign());
        Permanent attacker = addCreatureReady(player1, new SabertoothMauler());
        attacker.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChromeReplicator());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GarruksUprising());
        Permanent creature = addCreatureReady(player2, new PackLeader());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId(), enchantment.getId())
                .doesNotContain(creature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));

        harness.assertInGraveyard(player2, "Chrome Replicator");
        harness.assertOnBattlefield(player2, "Garruk's Uprising");
        harness.assertOnBattlefield(player2, "Pack Leader");
    }

    @Test
    @DisplayName("The combat-damage trigger fires once for multiple Cats")
    void combatDamageTriggerFiresOnceForMultipleCats() {
        addCreatureReady(player1, new FelineSovereign());
        Permanent firstCat = addCreatureReady(player1, new SabertoothMauler());
        firstCat.setAttacking(true);
        Permanent secondCat = addCreatureReady(player1, new SabertoothMauler());
        secondCat.setAttacking(true);
        harness.addToBattlefield(player2, new ChromeReplicator());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The artifact or enchantment target is chosen before the trigger resolves")
    void targetIsChosenBeforeResolution() {
        Permanent sovereign = addCreatureReady(player1, new FelineSovereign());
        sovereign.setAttacking(true);
        harness.addToBattlefield(player2, new ChromeReplicator());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.assertOnBattlefield(player2, "Chrome Replicator");
    }

    @Test
    @DisplayName("Feline Sovereign can trigger from its own combat damage and destroy an enchantment")
    void ownCombatDamageDestroysEnchantment() {
        Permanent sovereign = addCreatureReady(player1, new FelineSovereign());
        sovereign.setAttacking(true);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new ChromeReplicator());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GarruksUprising());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(enchantment.getId()).doesNotContain(ownArtifact.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(enchantment.getId()));

        harness.assertInGraveyard(player2, "Garruk's Uprising");
        harness.assertOnBattlefield(player1, "Chrome Replicator");
    }

    @Test
    @DisplayName("Feline Sovereign does not boost or protect itself or opposing Cats")
    void excludesSelfAndOpposingCats() {
        Permanent sovereign = addCreatureReady(player1, new FelineSovereign());
        Permanent opposingCat = addCreatureReady(player2, new FelineSovereign());
        Permanent dog = addCreatureReady(player2, new PackLeader());

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCat)).isEqualTo(3);
        assertThat(gqs.hasProtectionFromSource(gd, sovereign, dog)).isFalse();
        assertThat(gqs.hasProtectionFromSource(gd, opposingCat, dog)).isFalse();
    }

    @Test
    @DisplayName("A non-Cat dealing combat damage does not trigger destruction")
    void nonCatCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new FelineSovereign());
        Permanent attacker = addCreatureReady(player1, new PackLeader());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new GarruksUprising());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Garruk's Uprising");
    }
}
