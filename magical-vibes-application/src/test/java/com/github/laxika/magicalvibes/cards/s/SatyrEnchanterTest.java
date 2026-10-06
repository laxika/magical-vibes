package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.PsychicCorrosion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SatyrEnchanter.class, Forest.class, GrizzlyBears.class, HonorOfThePure.class,
        PsychicCorrosion.class, Murder.class})
class SatyrEnchanterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an enchantment spell triggers a draw")
    void drawsOnEnchantmentCast() {
        harness.addToBattlefield(player1, new SatyrEnchanter());
        harness.setHand(player1, List.of(new HonorOfThePure()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Satyr Enchanter"));

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Casting a non-enchantment spell does not trigger a draw")
    void doesNotDrawOnNonEnchantmentCast() {
        harness.addToBattlefield(player1, new SatyrEnchanter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("An opponent's enchantment spell does not trigger a draw")
    void doesNotDrawOnOpponentsEnchantmentCast() {
        harness.addToBattlefield(player1, new SatyrEnchanter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new HonorOfThePure()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castEnchantment(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("The draw resolves before the enchantment enters the battlefield")
    void drawsBeforeEnchantmentResolves() {
        harness.addToBattlefield(player1, new SatyrEnchanter());
        harness.setHand(player1, List.of(new PsychicCorrosion()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0);
        harness.assertNotInHand(player1, "Forest");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Psychic Corrosion");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Psychic Corrosion");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each Satyr Enchanter triggers independently")
    void multipleEnchantersEachDrawOneCard() {
        harness.addToBattlefield(player1, new SatyrEnchanter());
        harness.addToBattlefield(player1, new SatyrEnchanter());
        harness.setHand(player1, List.of(new PsychicCorrosion()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Psychic Corrosion");
    }

    @Test
    @DisplayName("Putting an enchantment onto the battlefield without casting does not trigger")
    void enchantmentEnteringWithoutBeingCastDoesNotDraw() {
        harness.addToBattlefield(player1, new SatyrEnchanter());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new PsychicCorrosion());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Psychic Corrosion");
    }

    @Test
    @DisplayName("The draw still resolves after Satyr Enchanter is destroyed")
    void drawSurvivesSourceRemoval() {
        var enchanter = harness.addToBattlefieldAndReturn(player1, new SatyrEnchanter());
        harness.setHand(player1, List.of(new PsychicCorrosion()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, enchanter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Satyr Enchanter");
        harness.assertNotInHand(player1, "Forest");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Psychic Corrosion");
    }
}
