package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CoastalPiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.PackLeader;
import com.github.laxika.magicalvibes.cards.r.RegalCaracal;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FelineSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("Other Cats get +1/+1 and protection from Dogs")
    void otherCatsGetBoostAndProtectionFromDogs() {
        Permanent cat = addCreatureReady(player1, new RegalCaracal());
        Permanent nonCat = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent attacker = addCreatureReady(player1, new RegalCaracal());
        attacker.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CoastalPiracy());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId(), enchantment.getId())
                .doesNotContain(creature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(enchantment.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The combat-damage trigger fires once for multiple Cats")
    void combatDamageTriggerFiresOnceForMultipleCats() {
        addCreatureReady(player1, new FelineSovereign());
        Permanent firstCat = addCreatureReady(player1, new RegalCaracal());
        firstCat.setAttacking(true);
        Permanent secondCat = addCreatureReady(player1, new RegalCaracal());
        secondCat.setAttacking(true);
        harness.addToBattlefield(player2, new LeoninScimitar());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
