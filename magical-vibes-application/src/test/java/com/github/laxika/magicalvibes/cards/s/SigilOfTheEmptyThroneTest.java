package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SigilOfTheEmptyThroneTest extends BaseCardTest {

    // ===== Trigger fires on enchantment cast =====

    @Test
    @DisplayName("Casting an enchantment spell creates a 4/4 white flying Angel token")
    void enchantmentCastCreatesAngel() {
        harness.addToBattlefield(player1, new SigilOfTheEmptyThrone());
        harness.setHand(player1, List.of(new HonorOfThePure()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        GameData gd = harness.getGameData();

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Sigil of the Empty Throne"));

        // Resolve triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel")
                        && p.getCard().isToken()
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().getColor() == CardColor.WHITE
                        && p.getCard().getSubtypes().contains(CardSubtype.ANGEL)
                        && p.getCard().getKeywords().contains(Keyword.FLYING)
                        && p.getCard().getPower() == 4
                        && p.getCard().getToughness() == 4);
    }

    // ===== Non-enchantment does not trigger =====

    @Test
    @DisplayName("Non-enchantment spell does not trigger Sigil of the Empty Throne")
    void nonEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new SigilOfTheEmptyThrone());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Opponent's enchantment does not trigger =====

    @Test
    @DisplayName("Opponent casting an enchantment does not trigger Sigil of the Empty Throne")
    void opponentEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new SigilOfTheEmptyThrone());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new HonorOfThePure()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castEnchantment(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Sigil of the Empty Throne"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    // ===== Multiple enchantment casts create multiple tokens =====

    @Test
    @DisplayName("Casting two enchantment spells creates two Angel tokens")
    void multipleEnchantmentCastsCreateMultipleTokens() {
        harness.addToBattlefield(player1, new SigilOfTheEmptyThrone());
        harness.setHand(player1, List.of(new HonorOfThePure(), new HonorOfThePure()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve token trigger
        harness.passBothPriorities(); // resolve the enchantment spell

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve token trigger
        harness.passBothPriorities(); // resolve the enchantment spell

        GameData gd = harness.getGameData();
        long angelCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Angel") && p.getCard().isToken())
                .count();
        assertThat(angelCount).isEqualTo(2);
    }
}
