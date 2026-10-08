package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AlexisCloak;
import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolitionReins.class, GrizzlyBears.class, FountainOfYouth.class, Demystify.class, AlexisCloak.class})
class VolitionReinsTest extends BaseCardTest {


    @Test
    @DisplayName("Resolving Volition Reins steals opponent's creature")
    void stealsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new VolitionReins()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
    }


    @Test
    @DisplayName("Resolving Volition Reins steals opponent's artifact")
    void stealsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new VolitionReins()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
    }


    @Test
    @DisplayName("Volition Reins untaps enchanted permanent if it was tapped")
    void untapsTappedPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        harness.setHand(player1, List.of(new VolitionReins()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Volition Reins does not untap enchanted permanent if it was already untapped")
    void doesNotUntapAlreadyUntappedPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        // creature is untapped by default

        harness.setHand(player1, List.of(new VolitionReins()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }


    @Test
    @DisplayName("Volition Reins fizzles if target is no longer on the battlefield")
    void fizzlesIfTargetGone() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new VolitionReins()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the creature before resolution
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        // Volition Reins should be in graveyard
        harness.assertInGraveyard(player1, "Volition Reins");
    }


    @Test
    @DisplayName("Creature returns to owner when Volition Reins is destroyed")
    void creatureReturnsWhenDestroyed() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new VolitionReins()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        // Creature should be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        // Destroy Volition Reins with Demystify
        Permanent volitionReinsPerm = findPermanent(player1, "Volition Reins");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, volitionReinsPerm.getId());

        // Creature should return to player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
    }

    @Test
    @DisplayName("No untap ability triggers when the enchanted permanent enters untapped")
    void untappedPermanentDoesNotCreateUntapTrigger() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new VolitionReins()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Volition Reins untaps an enchanted artifact")
    void untapsTappedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        artifact.tap();
        harness.setHand(player1, List.of(new VolitionReins()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
        assertThat(artifact.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Gaining shroud in response does not stop the nontargeting untap ability")
    void untapsPermanentThatGainsShroudInResponse() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new VolitionReins(), new AlexisCloak()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Alexi's Cloak");
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
