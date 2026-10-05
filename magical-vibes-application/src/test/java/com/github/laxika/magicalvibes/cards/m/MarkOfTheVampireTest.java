package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarkOfTheVampire.class, QueensBaySoldier.class, ElaborateFirecannon.class, Demystify.class})
class MarkOfTheVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature with Mark of the Vampire")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Mark of the Vampire")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addToBattlefield(player1, new ElaborateFirecannon());
        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent artifact = findPermanent(player1, "Elaborate Firecannon");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsPlusTwoPlusTwo() {
        Permanent creature = addCreatureReady(player1, new QueensBaySoldier());

        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Queen's Bay Soldier base 2/2 + 2/2 = 4/4
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature gains lifelink")
    void enchantedCreatureGainsLifelink() {
        Permanent creature = addCreatureReady(player1, new QueensBaySoldier());

        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and lifelink when Mark of the Vampire is removed")
    void creatureLosesBoostAndLifelinkWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new QueensBaySoldier());

        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Verify effects are applied
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();

        Permanent markPerm = findPermanent(player1, "Mark of the Vampire");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, markPerm.getId());

        // Back to base 2/2
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Can enchant an opponent's creature")
    void canEnchantOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new QueensBaySoldier());

        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Combat damage gains life for the enchanted creature's controller")
    void combatDamageGainsLife() {
        Permanent creature = addCreatureReady(player1, new QueensBaySoldier());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Enchanting an opponent's creature gives lifelink life to the opponent")
    void opposingCreatureControllerGainsLife() {
        Permanent creature = addCreatureReady(player2, new QueensBaySoldier());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("Multiple Marks stack their boosts but do not multiply lifelink")
    void multipleMarksDoNotMultiplyLifelink() {
        Permanent creature = addCreatureReady(player1, new QueensBaySoldier());
        Permanent other = addCreatureReady(player1, new QueensBaySoldier());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MarkOfTheVampire(), new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.LIFELINK)).isFalse();

        declareAttackers(player1, List.of(0));

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Mark does not resolve when its target dies in response")
    void targetDiesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new QueensBaySoldier());
        harness.addToBattlefield(player2, new ElaborateFirecannon());
        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Queen's Bay Soldier");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mark of the Vampire");
        harness.assertNotOnBattlefield(player1, "Mark of the Vampire");
        assertThat(gd.stack).isEmpty();
    }
}
