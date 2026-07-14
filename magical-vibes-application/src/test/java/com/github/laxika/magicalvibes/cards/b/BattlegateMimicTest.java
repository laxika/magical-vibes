package com.github.laxika.magicalvibes.cards.b;

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

class BattlegateMimicTest extends BaseCardTest {

    @BeforeEach
    void setUpTest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    /** A raw creature spell that is both red and white. */
    private Card redWhiteSpell() {
        Card card = new Card();
        card.setName("Boros Test Bear");
        card.setType(CardType.CREATURE);
        card.setManaCost("{R}{W}");
        card.setColor(CardColor.RED);
        card.setColors(List.of(CardColor.RED, CardColor.WHITE));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    /** A raw creature spell that is only red. */
    private Card redSpell() {
        Card card = new Card();
        card.setName("Mono Red Test Bear");
        card.setType(CardType.CREATURE);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        card.setColors(List.of(CardColor.RED));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Casting a red-and-white spell makes the Mimic 4/2 with first strike")
    void redWhiteSpellPumpsMimic() {
        Permanent mimic = addCreatureReady(player1, new BattlegateMimic());
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isFalse();

        harness.setHand(player1, List.of(redWhiteSpell()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(mimic.getEffectivePower()).isEqualTo(4);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Pump and first strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent mimic = addCreatureReady(player1, new BattlegateMimic());

        harness.setHand(player1, List.of(redWhiteSpell()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(mimic.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Casting a mono-red spell does not trigger the Mimic")
    void monoRedSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new BattlegateMimic());

        harness.setHand(player1, List.of(redSpell()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isFalse();
    }
}
