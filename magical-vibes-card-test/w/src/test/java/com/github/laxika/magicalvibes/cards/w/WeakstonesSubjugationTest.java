package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeakstonesSubjugation.class, GrizzlyBears.class, FountainOfYouth.class, Disenchant.class})
class WeakstonesSubjugationTest extends BaseCardTest {

    @Test
    void mayPayThreeToTapEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WeakstonesSubjugation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void decliningPaymentLeavesEnchantedCreatureUntapped() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WeakstonesSubjugation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void mayTargetAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new WeakstonesSubjugation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached()
                        && p.getAttachedTo().equals(artifact.getId()));
    }

    @Test
    void enchantedPermanentDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        Permanent aura = new Permanent(new WeakstonesSubjugation());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void paymentTapsArtifactAndPreventsItsUntap() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        otherArtifact.tap();
        harness.setHand(player1, List.of(new WeakstonesSubjugation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
        assertThat(artifact.isTapped()).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.performUntapStep(player2);
        assertThat(artifact.isTapped()).isTrue();
        assertThat(otherArtifact.isTapped()).isFalse();
    }

    @Test
    void decliningPaymentStillLocksCreatureUntilAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new WeakstonesSubjugation(), new Disenchant()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();

        Permanent aura = findPermanent(player1, "Weakstone's Subjugation");
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.assertInGraveyard(player1, "Weakstone's Subjugation");
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void cannotEnchantANonartifactEnchantment() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WeakstonesSubjugation());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new WeakstonesSubjugation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }
}
