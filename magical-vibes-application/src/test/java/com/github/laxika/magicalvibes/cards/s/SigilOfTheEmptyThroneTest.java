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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigilOfTheEmptyThrone.class, HonorOfThePure.class, GrizzlyBears.class})
class SigilOfTheEmptyThroneTest extends BaseCardTest {

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

    @Test
    @DisplayName("Sigil does not trigger for its own casting or entry")
    void castingSigilDoesNotTriggerItself() {
        harness.setHand(player1, List.of(new SigilOfTheEmptyThrone()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sigil of the Empty Throne");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("An existing Sigil triggers when a second Sigil is cast")
    void existingSigilTriggersForSecondSigil() {
        harness.addToBattlefield(player1, new SigilOfTheEmptyThrone());
        harness.setHand(player1, List.of(new SigilOfTheEmptyThrone()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Angel"))
                .hasSize(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Angel"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Sigil of the Empty Throne"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Each Sigil triggers independently for an enchantment cast")
    void multipleSigilsEachCreateAnAngel() {
        harness.addToBattlefield(player1, new SigilOfTheEmptyThrone());
        harness.addToBattlefield(player1, new SigilOfTheEmptyThrone());
        harness.setHand(player1, List.of(new SigilOfTheEmptyThrone()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).filteredOn(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Angel"))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An enchantment entering without being cast does not trigger Sigil")
    void enchantmentEnteringWithoutBeingCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new SigilOfTheEmptyThrone());

        harness.enterBattlefieldAndReturn(player1, new SigilOfTheEmptyThrone());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .noneMatch(p -> p.getCard().isToken());
    }
}
