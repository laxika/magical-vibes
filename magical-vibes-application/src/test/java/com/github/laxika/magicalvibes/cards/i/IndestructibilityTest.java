package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.OpenTheVaults;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Indestructibility.class, DoomBlade.class, Spellbook.class, RuneclawBear.class, Naturalize.class, OpenTheVaults.class})
class IndestructibilityTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Indestructibility targeting a creature puts it on the stack")
    void castingOnCreaturePutsOnStack() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Indestructibility()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Resolving Indestructibility attaches it to target creature")
    void resolvingAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Indestructibility()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Indestructibility")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Can cast Indestructibility targeting a noncreature permanent")
    void canTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.setHand(player1, List.of(new Indestructibility()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Enchanted creature has indestructible")
    void enchantedCreatureHasIndestructible() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        addAttachedAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted noncreature permanent has indestructible")
    void enchantedNonCreatureHasIndestructible() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        addAttachedAura(player1, artifact);

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature survives destroy effect")
    void enchantedCreatureSurvivesDestroy() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        addAttachedAura(player1, creature);

        // Cast Doom Blade targeting the indestructible creature
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should survive
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Enchanted creature survives lethal damage")
    void enchantedCreatureSurvivesLethalDamage() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        addAttachedAura(player1, creature);

        // Mark 10 damage on the 2/2 creature
        creature.setMarkedDamage(10);
        harness.runStateBasedActions();

        // Creature should survive because it's indestructible
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Permanent loses indestructible when Indestructibility is removed")
    void losesIndestructibleWhenAuraRemoved() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent aura = addAttachedAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructibility does not affect other permanents")
    void doesNotAffectOtherPermanents() {
        Permanent creature1 = addCreatureReady(player1, new RuneclawBear());
        Permanent creature2 = addCreatureReady(player1, new RuneclawBear());
        addAttachedAura(player1, creature1);

        assertThat(gqs.hasKeyword(gd, creature1, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("An opposing noncreature permanent survives Naturalize after the Aura resolves")
    void opposingArtifactSurvivesDestroy() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new Indestructibility()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Indestructibility");
        assertThat(findPermanent(player1, "Indestructibility").getAttachedTo()).isEqualTo(artifact.getId());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertNotInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("Destroying the Aura removes protection and lethal damage destroys the creature")
    void destroyingAuraExposesLethalDamage() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent aura = addAttachedAura(player1, creature);
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Runeclaw Bear");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Indestructibility");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Open the Vaults can return Indestructibility attached to a noncreature permanent")
    void returnsFromGraveyardOntoNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setGraveyard(player1, List.of(new Indestructibility()));
        harness.setHand(player1, List.of(new OpenTheVaults()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.assertOnBattlefield(player1, "Indestructibility");
        harness.assertNotInGraveyard(player1, "Indestructibility");
        assertThat(findPermanent(player1, "Indestructibility").getAttachedTo()).isEqualTo(artifact.getId());
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    private Permanent addAttachedAura(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, new Indestructibility());
        aura.setAttachedTo(target.getId());
        return aura;
    }
}
