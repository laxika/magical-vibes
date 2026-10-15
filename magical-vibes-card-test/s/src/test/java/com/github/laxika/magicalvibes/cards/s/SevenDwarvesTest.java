package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.carddata.DeckLegalityRegistry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeckDefinition;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.DeckValidationService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@CardUsed({SevenDwarves.class, GrizzlyBears.class, WitnessProtection.class})
class SevenDwarvesTest extends BaseCardTest {

    @Test
    @DisplayName("Seven Dwarves is 2/2 when it is the only one")
    void isBaseStatsAlone() {
        Permanent dwarves = addCreatureReady(player1, new SevenDwarves());

        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarves)).isEqualTo(2);
    }

    @Test
    @DisplayName("Seven Dwarves gets +1/+1 for each other Seven Dwarves you control")
    void countsOwnOtherDwarves() {
        Permanent dwarves = addCreatureReady(player1, new SevenDwarves());
        harness.addToBattlefield(player1, new SevenDwarves());
        harness.addToBattlefield(player1, new SevenDwarves());

        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dwarves)).isEqualTo(4);
    }

    @Test
    @DisplayName("Seven Dwarves does not count opponents' Seven Dwarves")
    void ignoresOpponentDwarves() {
        Permanent dwarves = addCreatureReady(player1, new SevenDwarves());
        harness.addToBattlefield(player2, new SevenDwarves());
        harness.addToBattlefield(player2, new SevenDwarves());

        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarves)).isEqualTo(2);
    }

    @Test
    @DisplayName("Seven Dwarves does not count creatures with different names")
    void ignoresDifferentNames() {
        Permanent dwarves = addCreatureReady(player1, new SevenDwarves());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarves)).isEqualTo(2);
    }

    @Test
    @DisplayName("Seven Dwarves bonus shrinks when another Seven Dwarves leaves the battlefield")
    void bonusUpdatesWhenAnotherDwarfLeaves() {
        Permanent dwarves = addCreatureReady(player1, new SevenDwarves());
        harness.addToBattlefield(player1, new SevenDwarves());
        harness.addToBattlefield(player1, new SevenDwarves());

        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> !p.getId().equals(dwarves.getId()) && p.getCard().getName().equals("Seven Dwarves"));

        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarves)).isEqualTo(2);
    }

    @Test
    void doesNotCountAnotherDwarfWhoseNameHasChanged() {
        Permanent dwarves = harness.addToBattlefieldAndReturn(player1, new SevenDwarves());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SevenDwarves());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new WitnessProtection());
        aura.setAttachedTo(other.getId());

        assertThat(gqs.getEffectiveName(gd, other)).isEqualTo("Legitimate Businessperson");
        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarves)).isEqualTo(2);
    }

    @Test
    void eachDwarfReceivesTheBonusFromTheOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SevenDwarves());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SevenDwarves());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void deckMayContainSevenDwarves() {
        List<Card> cards = IntStream.range(0, 7).mapToObj(i -> (Card) new SevenDwarves()).toList();
        DeckValidationService validator = new DeckValidationService(mock(DeckLegalityRegistry.class));

        assertThat(validator.validate(new DeckDefinition(cards, List.of(), null), DeckFormat.CASUAL).valid())
                .isTrue();
    }

    @Test
    void deckMayNotContainEightDwarves() {
        List<Card> cards = IntStream.range(0, 8).mapToObj(i -> (Card) new SevenDwarves()).toList();
        DeckValidationService validator = new DeckValidationService(mock(DeckLegalityRegistry.class));

        assertThat(validator.validate(new DeckDefinition(cards, List.of(), null), DeckFormat.CASUAL).errors())
                .containsExactly("Seven Dwarves: maximum 7 copies across the deck and sideboard.");
    }

    @Test
    void sevenCopyLimitIncludesTheSideboard() {
        List<Card> cards = IntStream.range(0, 7).mapToObj(i -> (Card) new SevenDwarves()).toList();
        DeckValidationService validator = new DeckValidationService(mock(DeckLegalityRegistry.class));

        assertThat(validator.validate(new DeckDefinition(cards, List.of(new SevenDwarves()), null),
                DeckFormat.CASUAL).errors())
                .containsExactly("Seven Dwarves: maximum 7 copies across the deck and sideboard.");
    }
}
