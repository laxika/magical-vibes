package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarkOfTheVampireTest extends BaseCardTest {

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Mark of the Vampire")
    void canTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Mark of the Vampire")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Static boost =====

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsPlusTwoPlusTwo() {
        Permanent creature = addCreature(player1);

        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Grizzly Bears base 2/2 + 2/2 = 4/4
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    // ===== Lifelink grant =====

    @Test
    @DisplayName("Enchanted creature gains lifelink")
    void enchantedCreatureGainsLifelink() {
        Permanent creature = addCreature(player1);

        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    // ===== Removal =====

    @Test
    @DisplayName("Creature loses boost and lifelink when Mark of the Vampire is removed")
    void creatureLosesBoostAndLifelinkWhenRemoved() {
        Permanent creature = addCreature(player1);

        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Verify effects are applied
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();

        Permanent markPerm = findPermanentByName(player1, "Mark of the Vampire");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, markPerm.getId());
        harness.passBothPriorities();

        // Back to base 2/2
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    // ===== Can enchant opponent's creature =====

    @Test
    @DisplayName("Can enchant an opponent's creature")
    void canEnchantOpponentCreature() {
        Permanent opponentCreature = addCreature(player2);

        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.LIFELINK)).isTrue();
    }

    // ===== Helper methods =====

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanentByName(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
