package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InBolassClutches.class, InvokeTheDivine.class, ShortSword.class, PrimordialWurm.class, Forest.class})
class InBolassClutchesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving In Bolas's Clutches steals opponent's creature")
    void stealsCreature() {
        Permanent creature = addCreatureReady(player2, new PrimordialWurm());

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
    }

    @Test
    @DisplayName("Resolving In Bolas's Clutches steals opponent's artifact")
    void stealsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ShortSword());

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Enchanted permanent gains legendary supertype via static bonus")
    void enchantedPermanentBecomesLegendary() {
        Permanent creature = addCreatureReady(player2, new PrimordialWurm());

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Verify the static bonus grants legendary
        var bonus = gqs.computeStaticBonus(gd, creature);
        assertThat(bonus.grantedSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("In Bolas's Clutches fizzles if target is no longer on the battlefield")
    void fizzlesIfTargetGone() {
        Permanent creature = addCreatureReady(player2, new PrimordialWurm());

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the creature before resolution
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        // In Bolas's Clutches should be in graveyard
        harness.assertInGraveyard(player1, "In Bolas's Clutches");
    }

    @Test
    @DisplayName("Creature returns to owner when In Bolas's Clutches is destroyed")
    void creatureReturnsWhenDestroyed() {
        Permanent creature = addCreatureReady(player2, new PrimordialWurm());

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        // Destroy In Bolas's Clutches with Invoke the Divine
        Permanent auraPerm = findPermanent(player1, "In Bolas's Clutches");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new InvokeTheDivine()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player2, 0, auraPerm.getId());

        // Creature should return to player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
    }

    @Test
    @DisplayName("Legendary supertype is removed when In Bolas's Clutches is destroyed")
    void legendaryRemovedWhenDestroyed() {
        Permanent creature = addCreatureReady(player2, new PrimordialWurm());

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Verify legendary is granted
        var bonus = gqs.computeStaticBonus(gd, creature);
        assertThat(bonus.grantedSupertypes()).contains(CardSupertype.LEGENDARY);

        // Destroy the aura
        Permanent auraPerm = findPermanent(player1, "In Bolas's Clutches");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new InvokeTheDivine()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player2, 0, auraPerm.getId());

        // Creature should no longer have legendary supertype
        var bonusAfter = gqs.computeStaticBonus(gd, creature);
        assertThat(bonusAfter.grantedSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("A basic land can be stolen and becomes legendary while remaining basic")
    void stealsBasicLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gqs.hasEffectiveSupertype(gd, land, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, land, CardSupertype.BASIC)).isTrue();
    }

    @Test
    @DisplayName("An enchanted permanent can coexist with a nonlegendary permanent of the same name")
    void onlyEnchantedPermanentIsLegendary() {
        Permanent unenchanted = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSupertype(gd, enchanted, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, unenchanted, CardSupertype.LEGENDARY)).isFalse();
        assertThat(countPermanents(player1, "Primordial Wurm")).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Destroying the Aura restores control and removes legendary from a noncreature permanent")
    void artifactReturnsWhenDestroyed() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ShortSword());
        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasEffectiveSupertype(gd, artifact, CardSupertype.LEGENDARY)).isTrue();

        Permanent aura = findPermanent(player1, "In Bolas's Clutches");
        harness.setHand(player2, List.of(new InvokeTheDivine()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0, aura.getId());

        harness.assertOnBattlefield(player2, "Short Sword");
        harness.assertNotOnBattlefield(player1, "Short Sword");
        assertThat(gqs.hasEffectiveSupertype(gd, artifact, CardSupertype.LEGENDARY)).isFalse();
    }

    @Test
    @DisplayName("Keeping a second Clutches under the legend rule ends the first Aura's effects")
    void legendRuleRemovalEndsFirstAuraEffects() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new ShortSword());
        harness.setHand(player1, List.of(new InBolassClutches(), new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 12);
        harness.castEnchantment(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        Permanent firstAura = findPermanent(player1, "In Bolas's Clutches");
        harness.castEnchantment(player1, 0, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.LegendRule.class);
        Permanent secondAura = findPermanents(player1, "In Bolas's Clutches").stream()
                .filter(p -> !p.getId().equals(firstAura.getId())).findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, secondAura.getId());

        harness.assertInGraveyard(player1, "In Bolas's Clutches");
        harness.assertOnBattlefield(player2, "Primordial Wurm");
        harness.assertOnBattlefield(player1, "Short Sword");
        assertThat(gqs.hasEffectiveSupertype(gd, firstTarget, CardSupertype.LEGENDARY)).isFalse();
        assertThat(gqs.hasEffectiveSupertype(gd, secondTarget, CardSupertype.LEGENDARY)).isTrue();
    }
}
