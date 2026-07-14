package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiverfallMimicTest extends BaseCardTest {

    @BeforeEach
    void setUpTest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    /** A raw creature spell that is both blue and red. */
    private Card blueRedSpell() {
        Card card = new Card();
        card.setName("Izzet Test Bear");
        card.setType(CardType.CREATURE);
        card.setManaCost("{U}{R}");
        card.setColor(CardColor.BLUE);
        card.setColors(List.of(CardColor.BLUE, CardColor.RED));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    /** A raw creature spell that is only blue. */
    private Card monoBlueSpell() {
        Card card = new Card();
        card.setName("Blue Test Bear");
        card.setType(CardType.CREATURE);
        card.setManaCost("{U}");
        card.setColor(CardColor.BLUE);
        card.setColors(List.of(CardColor.BLUE));
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    @Test
    @DisplayName("Casting a blue-and-red spell makes the Mimic 3/3 and unblockable")
    void blueRedSpellPumpsMimic() {
        Permanent mimic = addCreatureReady(player1, new RiverfallMimic());
        assertThat(mimic.isCantBeBlocked()).isFalse();

        harness.setHand(player1, List.of(blueRedSpell()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(mimic.getEffectivePower()).isEqualTo(3);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(3);
        assertThat(mimic.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Pump and unblockable wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent mimic = addCreatureReady(player1, new RiverfallMimic());

        harness.setHand(player1, List.of(blueRedSpell()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(mimic.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(mimic.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Casting a mono-blue spell does not trigger the Mimic")
    void monoBlueSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new RiverfallMimic());

        harness.setHand(player1, List.of(monoBlueSpell()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(mimic.isCantBeBlocked()).isFalse();
    }
}
