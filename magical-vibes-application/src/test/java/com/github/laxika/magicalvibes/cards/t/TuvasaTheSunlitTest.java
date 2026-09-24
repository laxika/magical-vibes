package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MentalDiscipline;
import com.github.laxika.magicalvibes.cards.e.Exploration;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        harness.addToBattlefield(player1, new TuvasaTheSunlit());
        harness.addToBattlefield(player1, new MentalDiscipline());
        harness.addToBattlefield(player1, new MentalDiscipline());
        harness.addToBattlefield(player2, new MentalDiscipline());

        Permanent tuvasa = findPermanent(player1, "Tuvasa the Sunlit");
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
}
