package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

class NightskyMimicTest extends BaseCardTest {

    @BeforeEach
    void setUpTest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    /** A raw creature spell that is both white and black. */
    private Card whiteBlackSpell() {
        Card card = new Card();
        card.setName("Orzhov Test Bear");
        card.setType(CardType.CREATURE);
        card.setManaCost("{W}{B}");
        card.setColor(CardColor.WHITE);
        card.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Casting a white-and-black spell makes the Mimic 4/4 with flying")
    void whiteBlackSpellPumpsMimic() {
        Permanent mimic = addCreatureReady(player1, new NightskyMimic());
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isFalse();

        harness.setHand(player1, List.of(whiteBlackSpell()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(mimic.getEffectivePower()).isEqualTo(4);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Pump and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent mimic = addCreatureReady(player1, new NightskyMimic());

        harness.setHand(player1, List.of(whiteBlackSpell()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(mimic.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Casting a mono-white spell does not trigger the Mimic")
    void monoWhiteSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new NightskyMimic());

        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FLYING)).isFalse();
    }
}
