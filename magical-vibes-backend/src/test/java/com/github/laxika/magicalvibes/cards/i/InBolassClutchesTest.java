package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InBolassClutchesTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("In Bolas's Clutches has correct effects")
    void hasCorrectEffects() {
        InBolassClutches card = new InBolassClutches();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anySatisfy(e -> assertThat(e).isInstanceOf(ControlEnchantedCreatureEffect.class))
                .anySatisfy(e -> assertThat(e).isInstanceOf(GrantSupertypeToEnchantedPermanentEffect.class));
    }

    // ===== Stealing creatures =====

    @Test
    @DisplayName("Resolving In Bolas's Clutches steals opponent's creature")
    void stealsCreature() {
        Permanent creature = addCreatureReady(player2);

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

    // ===== Stealing noncreature permanents =====

    @Test
    @DisplayName("Resolving In Bolas's Clutches steals opponent's artifact")
    void stealsArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
    }

    // ===== Enchanted permanent is legendary =====

    @Test
    @DisplayName("Enchanted permanent gains legendary supertype via static bonus")
    void enchantedPermanentBecomesLegendary() {
        Permanent creature = addCreatureReady(player2);

        // Grizzly Bears is not legendary
        assertThat(creature.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Verify the static bonus grants legendary
        var bonus = gqs.computeStaticBonus(gd, creature);
        assertThat(bonus.grantedSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("In Bolas's Clutches fizzles if target is no longer on the battlefield")
    void fizzlesIfTargetGone() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the creature before resolution
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        // In Bolas's Clutches should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("In Bolas's Clutches"));
    }

    // ===== Permanent returns when aura leaves =====

    @Test
    @DisplayName("Creature returns to owner when In Bolas's Clutches is destroyed")
    void creatureReturnsWhenDestroyed() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        // Destroy In Bolas's Clutches with Demystify
        Permanent auraPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("In Bolas's Clutches"))
                .findFirst().orElseThrow();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, auraPerm.getId());
        harness.passBothPriorities();

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
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Verify legendary is granted
        var bonus = gqs.computeStaticBonus(gd, creature);
        assertThat(bonus.grantedSupertypes()).contains(CardSupertype.LEGENDARY);

        // Destroy the aura
        Permanent auraPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("In Bolas's Clutches"))
                .findFirst().orElseThrow();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, auraPerm.getId());
        harness.passBothPriorities();

        // Creature should no longer have legendary supertype
        var bonusAfter = gqs.computeStaticBonus(gd, creature);
        assertThat(bonusAfter.grantedSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
