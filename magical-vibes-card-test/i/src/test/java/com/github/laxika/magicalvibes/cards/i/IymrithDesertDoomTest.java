package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BullsStrength;
import com.github.laxika.magicalvibes.cards.c.CleverConjurer;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IymrithDesertDoom.class, BullsStrength.class, CleverConjurer.class, HillGiantHerdgorger.class})
class IymrithDesertDoomTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped Iymrith has ward 4")
    void untappedIymrithHasWardFour() {
        Permanent iymrith = addReadyIymrith();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new BullsStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castInstant(player2, 0, iymrith.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Bull's Strength");
        assertThat(gqs.getEffectivePower(gd, iymrith)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, iymrith)).isEqualTo(5);
    }

    @Test
    @DisplayName("Tapped Iymrith does not have ward")
    void tappedIymrithDoesNotHaveWard() {
        Permanent iymrith = addReadyIymrith();
        iymrith.tap();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new BullsStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, iymrith.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, iymrith)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, iymrith)).isEqualTo(7);
        assertThat(iymrith.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Combat damage draws up to three cards based on the resulting hand size")
    void combatDamageDrawsToThreeCards() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger(), new HillGiantHerdgorger()));
        Permanent iymrith = addReadyIymrith();
        iymrith.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Paying ward allows the opponent's spell to resolve")
    void payingWardAllowsSpellToResolve() {
        Permanent iymrith = addReadyIymrith();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new BullsStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castInstant(player2, 0, iymrith.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, iymrith)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, iymrith)).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller's own spell does not trigger ward")
    void ownSpellDoesNotTriggerWard() {
        Permanent iymrith = addReadyIymrith();
        harness.setHand(player1, List.of(new BullsStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, iymrith.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, iymrith)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, iymrith)).isEqualTo(7);
    }

    @Test
    @DisplayName("Tapping Iymrith after ward triggers does not remove the trigger")
    void wardStillCountersAfterSourceBecomesTapped() {
        Permanent iymrith = addReadyIymrith();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new BullsStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castInstant(player2, 0, iymrith.getId());
        iymrith.tap();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(iymrith.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, iymrith)).isEqualTo(5);
        harness.assertInGraveyard(player2, "Bull's Strength");
    }

    @Test
    @DisplayName("Untapping Iymrith after it was targeted does not retroactively trigger ward")
    void untappingAfterTargetingDoesNotTriggerWard() {
        Permanent iymrith = addReadyIymrith();
        iymrith.tap();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new BullsStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, iymrith.getId());
        iymrith.untap();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, iymrith)).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ward counters an opponent's activated ability even if Iymrith is tapped afterward")
    void wardCountersOpponentActivatedAbility() {
        Permanent iymrith = addReadyIymrith();
        addCreatureReady(player2, new CleverConjurer());
        prepareOpponentTurn();
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, iymrith.getId());
        assertThat(gd.stack).hasSize(2);
        iymrith.tap();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(iymrith.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @DisplayName("Combat damage always draws one card, then only fills a hand below three")
    void combatDamageDrawsAccordingToHandSize(int initialHandSize) {
        harness.setHand(player1, fillerCards(initialHandSize));
        harness.setLibrary(player1, fillerCards(8));
        Permanent iymrith = addReadyIymrith();
        iymrith.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        int expectedHandSize = Math.max(3, initialHandSize + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(expectedHandSize);
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(8 - (expectedHandSize - initialHandSize));
        harness.assertLife(player2, 15);
    }

    private List<Card> fillerCards(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> (Card) new HillGiantHerdgorger())
                .toList();
    }

    private Permanent addReadyIymrith() {
        return addCreatureReady(player1, new IymrithDesertDoom());
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
