package com.github.laxika.magicalvibes.cards.w;

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

class WoodlurkerMimicTest extends BaseCardTest {

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
    @DisplayName("Casting a black-and-green spell makes the Mimic 4/5 with wither")
    void blackGreenSpellPumpsMimic() {
        Permanent mimic = addCreatureReady(player1, new WoodlurkerMimic());
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.WITHER)).isFalse();

        harness.setHand(player1, List.of(spell("{B}{G}", List.of(CardColor.BLACK, CardColor.GREEN))));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(mimic.getEffectivePower()).isEqualTo(4);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("Pump and wither wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent mimic = addCreatureReady(player1, new WoodlurkerMimic());

        harness.setHand(player1, List.of(spell("{B}{G}", List.of(CardColor.BLACK, CardColor.GREEN))));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(mimic.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("Casting a mono-green spell does not trigger the Mimic")
    void monoGreenSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new WoodlurkerMimic());

        harness.setHand(player1, List.of(spell("{G}", List.of(CardColor.GREEN))));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.WITHER)).isFalse();
    }
}
