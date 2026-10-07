package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MentalDiscipline;
import com.github.laxika.magicalvibes.cards.e.Exploration;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TuvasaTheSunlit.class, MentalDiscipline.class, Exploration.class,
        GrizzlyBears.class, Forest.class})
class TuvasaTheSunlitTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each enchantment its controller controls")
    void getsBoostForControlledEnchantments() {
        Permanent tuvasa = harness.addToBattlefieldAndReturn(player1, new TuvasaTheSunlit());
        harness.addToBattlefield(player1, new MentalDiscipline());
        harness.addToBattlefield(player1, new MentalDiscipline());
        harness.addToBattlefield(player2, new MentalDiscipline());

        assertThat(gqs.getEffectivePower(gd, tuvasa)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tuvasa)).isEqualTo(3);
    }

    @Test
    @DisplayName("Draws only for the first enchantment spell each turn")
    void drawsOnlyForFirstEnchantmentSpellEachTurn() {
        harness.addToBattlefield(player1, new TuvasaTheSunlit());
        harness.setHand(player1, List.of(new Exploration(), new Exploration()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("A non-enchantment spell does not use the enchantment trigger")
    void nonEnchantmentSpellDoesNotUseTrigger() {
        harness.addToBattlefield(player1, new TuvasaTheSunlit());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Exploration()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Does not draw for a second enchantment when Tuvasa entered after the first")
    void earlierEnchantmentCastStillCountsBeforeTuvasaEnters() {
        harness.setHand(player1, List.of(new Exploration(), new TuvasaTheSunlit(), new Exploration()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Drawing resolves before the enchantment enters and increases Tuvasa's size")
    void drawResolvesBeforeEnchantment() {
        Permanent tuvasa = harness.addToBattlefieldAndReturn(player1, new TuvasaTheSunlit());
        harness.setHand(player1, List.of(new Exploration()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Exploration");
        assertThat(gqs.getEffectivePower(gd, tuvasa)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tuvasa)).isEqualTo(1);

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Exploration");
        assertThat(gqs.getEffectivePower(gd, tuvasa)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tuvasa)).isEqualTo(2);
    }

    @Test
    @DisplayName("An enchantment entering without being cast does not draw or consume the first cast")
    void enchantmentEnteringWithoutCastDoesNotConsumeTrigger() {
        harness.addToBattlefield(player1, new TuvasaTheSunlit());
        harness.setHand(player1, List.of(new Exploration()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.enterBattlefieldAndReturn(player1, new Exploration());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Forest");
    }

    @Test
    @DisplayName("An opponent's enchantment spell does not draw a card")
    void opponentEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new TuvasaTheSunlit());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Exploration()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can draw again for the first enchantment on a later turn")
    void firstEnchantmentOnLaterTurnDrawsAgain() {
        harness.addToBattlefield(player1, new TuvasaTheSunlit());
        harness.setHand(player1, List.of(new Exploration()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Forest");

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Exploration()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Forest");
    }
}
