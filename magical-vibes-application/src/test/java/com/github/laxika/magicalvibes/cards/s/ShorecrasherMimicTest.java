package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShorecrasherMimic.class, Card.class})
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

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

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

    @Test
    @DisplayName("Hybrid mana paid with only green still triggers before the spell resolves")
    void hybridSpellTriggersBeforeResolution() {
        Permanent mimic = addCreatureReady(player1, new ShorecrasherMimic());
        harness.setHand(player1, List.of(new ShorecrasherMimic()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(mimic.getEffectivePower()).isEqualTo(5);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.TRAMPLE)).isTrue();

        harness.passBothPriorities();
        Permanent enteringMimic = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(mimic.getId()))
                .findFirst().orElseThrow();
        assertThat(enteringMimic.getEffectivePower()).isEqualTo(2);
        assertThat(enteringMimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, enteringMimic, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's green-and-blue spell does not trigger the Mimic")
    void opponentsSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new ShorecrasherMimic());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ShorecrasherMimic()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.TRAMPLE)).isFalse();
    }
}
