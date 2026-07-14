package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShorecrasherMimicTest extends BaseCardTest {

    @BeforeEach
    void setUpTest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    /** A raw creature spell of the given colors. */
    private Card spell(String cost, List<CardColor> colors) {
        Card card = new Card();
        card.setName("Test Bear");
        card.setType(CardType.CREATURE);
        card.setManaCost(cost);
        card.setColor(colors.get(0));
        card.setColors(colors);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Casting a green-and-blue spell makes the Mimic 5/3 with trample")
    void greenBlueSpellPumpsMimic() {
        Permanent mimic = addCreatureReady(player1, new ShorecrasherMimic());
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.TRAMPLE)).isFalse();

        harness.setHand(player1, List.of(spell("{G}{U}", List.of(CardColor.GREEN, CardColor.BLUE))));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(mimic.getEffectivePower()).isEqualTo(5);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Pump and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent mimic = addCreatureReady(player1, new ShorecrasherMimic());

        harness.setHand(player1, List.of(spell("{G}{U}", List.of(CardColor.GREEN, CardColor.BLUE))));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(mimic.getEffectivePower()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Casting a mono-blue spell does not trigger the Mimic")
    void monoBlueSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new ShorecrasherMimic());

        harness.setHand(player1, List.of(spell("{U}", List.of(CardColor.BLUE))));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.TRAMPLE)).isFalse();
    }
}
