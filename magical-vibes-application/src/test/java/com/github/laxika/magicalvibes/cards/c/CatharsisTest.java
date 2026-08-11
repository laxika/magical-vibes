package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatharsisTest extends BaseCardTest {

    @Test
    @DisplayName("Two white mana spent: creates two Kithkin Soldier tokens")
    void twoWhiteManaCreatesTokens() {
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kithkin Soldier")).hasSize(2);
        assertThat(findPermanent(player1, "Catharsis").getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Two red mana spent: creatures you control get +1/+1 and haste")
    void twoRedManaBoostsAndHastesCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        Permanent catharsis = findPermanent(player1, "Catharsis");
        assertThat(catharsis.getEffectivePower()).isEqualTo(4);
        assertThat(catharsis.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, catharsis, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("One mana of each color does not satisfy either double-color clause")
    void oneOfEachColorDoesNotSatisfyDoubleColorClauses() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kithkin Soldier")).isEmpty();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Two mana of each color spent: both ETB clauses apply")
    void twoOfEachColorAppliesBothClauses() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kithkin Soldier")).hasSize(2);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Evoke with two white mana creates tokens and sacrifices Catharsis")
    void evokeCreatesTokensAndSacrificesSelf() {
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kithkin Soldier")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Catharsis");
        harness.assertInGraveyard(player1, "Catharsis");
    }
}
