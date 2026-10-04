package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmptyTheWarrens.class, Cancel.class})
class EmptyTheWarrensTest extends BaseCardTest {

    @Test
    @DisplayName("Cast creates two 1/1 Goblin tokens")
    void createsTwoGoblinTokens() {
        castEmptyTheWarrens();
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Goblin");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Storm copies the spell once for each spell cast before it this turn")
    void stormCopiesForEachPriorSpell() {
        GameData gd = harness.getGameData();
        gd.recordSpellCast(player1.getId(), new EmptyTheWarrens());
        gd.recordSpellCast(player2.getId(), new EmptyTheWarrens());

        castEmptyTheWarrens();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(6);
    }

    @Test
    @DisplayName("Storm survives countering the original and excludes spells cast after it")
    void stormSurvivesCounteringOriginal() {
        castEmptyTheWarrens();
        resolveAllTriggers();

        EmptyTheWarrens original = new EmptyTheWarrens();
        harness.castFromHand(player1, original, "{3}{R}");
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, original.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCopy()).isTrue();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(4);
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("Storm copies are not casts and do not increase subsequent storm counts")
    void copiesDoNotIncreaseLaterStormCounts() {
        castEmptyTheWarrens();
        resolveAllTriggers();
        castEmptyTheWarrens();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Goblin")).hasSize(6);

        castEmptyTheWarrens();
        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(12);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private void castEmptyTheWarrens() {
        harness.castFromHand(player1, new EmptyTheWarrens(), "{3}{R}");
    }
}
