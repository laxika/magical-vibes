package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.IronShieldElf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({Catharsis.class, IronShieldElf.class})
class CatharsisTest extends BaseCardTest {

    @Test
    @DisplayName("Two white mana spent: creates two Kithkin tokens")
    void twoWhiteManaCreatesTokens() {
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Kithkin")).hasSize(2);
        assertThat(findPermanent(player1, "Catharsis").getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Two red mana spent: creatures you control get +1/+1 and haste")
    void twoRedManaBoostsAndHastesCreatures() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new IronShieldElf());
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(elf.getEffectivePower()).isEqualTo(4);
        assertThat(elf.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.HASTE)).isTrue();
        Permanent catharsis = findPermanent(player1, "Catharsis");
        assertThat(catharsis.getEffectivePower()).isEqualTo(4);
        assertThat(catharsis.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, catharsis, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("One mana of each color does not satisfy either double-color clause")
    void oneOfEachColorDoesNotSatisfyDoubleColorClauses() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new IronShieldElf());
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Kithkin")).isEmpty();
        assertThat(elf.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Two mana of each color spent: both ETB clauses apply")
    void twoOfEachColorAppliesBothClauses() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new IronShieldElf());
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Kithkin")).hasSize(2);
        assertThat(elf.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Evoke with two white mana creates tokens and sacrifices Catharsis")
    void evokeCreatesTokensAndSacrificesSelf() {
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveEvokeTriggers();

        assertThat(findPermanents(player1, "Kithkin")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Catharsis");
        harness.assertInGraveyard(player1, "Catharsis");
    }
    @Test
    void whiteTriggerCreatesOracleTokens() {
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreatureWithEvoke(player1, 0, null);
        resolveEvokeTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getName()).isEqualTo("Kithkin");
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.KITHKIN);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        }
    }

    @Test
    void bothColorAbilitiesUseSeparateStackEntries() {
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void redEvokeBoostsOnlyOwnExistingCreaturesAndSacrificesCatharsis() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new IronShieldElf());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new IronShieldElf());
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreatureWithEvoke(player1, 0, null);
        resolveEvokeTriggers();

        assertThat(own.getEffectivePower()).isEqualTo(4);
        assertThat(own.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, own, Keyword.HASTE)).isTrue();
        assertThat(opposing.getEffectivePower()).isEqualTo(3);
        assertThat(opposing.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.HASTE)).isFalse();
        harness.assertInGraveyard(player1, "Catharsis");
        harness.assertNotOnBattlefield(player1, "Catharsis");

        Permanent later = harness.addToBattlefieldAndReturn(player1, new IronShieldElf());
        assertThat(later.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, later, Keyword.HASTE)).isFalse();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(own.getEffectivePower()).isEqualTo(3);
        assertThat(own.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, own, Keyword.HASTE)).isFalse();
    }

    @Test
    void mixedColorEvokeDoesNotTriggerEitherColorAbility() {
        harness.setHand(player1, List.of(new Catharsis()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreatureWithEvoke(player1, 0, null);
        resolveEvokeTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Catharsis");
    }
    private void resolveEvokeTriggers() {
        resolveAllTriggers();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (choice != null && choice.context() instanceof ChoiceContext.SpellCastTriggerOrder) {
            harness.handleListChoice(player1, choice.options().getFirst());
            resolveAllTriggers();
        }
    }
}
