package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AltarOfThePantheon;
import com.github.laxika.magicalvibes.cards.a.ArchonOfJustice;
import com.github.laxika.magicalvibes.cards.h.HeartlashCinder;
import com.github.laxika.magicalvibes.cards.r.RestlessApparition;
import com.github.laxika.magicalvibes.cards.u.Unmake;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpringjackShepherd.class, ArchonOfJustice.class, HeartlashCinder.class,
        AltarOfThePantheon.class, RestlessApparition.class, Unmake.class})
class SpringjackShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a Goat for each white mana symbol among your permanents (self included)")
    void etbCreatesGoatPerWhiteSymbol() {
        // Archon of Justice {3}{W}{W} = 2 white symbols; Heartlash Cinder {1}{R} = 0.
        // Springjack Shepherd itself {3}{W} = 1 (on the battlefield when the trigger resolves). Total = 3.
        addCreatureReady(player1, new ArchonOfJustice());
        addCreatureReady(player1, new HeartlashCinder());

        harness.setHand(player1, List.of(new SpringjackShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Goat");
        assertThat(tokens).hasSize(3);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOAT);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
    }

    @Test
    @DisplayName("Non-white permanents contribute nothing; only the Shepherd's own white symbol counts")
    void etbCountsOnlyWhiteSymbols() {
        addCreatureReady(player1, new HeartlashCinder()); // {1}{R} = 0 white symbols

        harness.setHand(player1, List.of(new SpringjackShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).hasSize(1);
    }

    @Test
    @DisplayName("Opponent's white permanents do not contribute")
    void etbIgnoresOpponentsWhiteSymbols() {
        addCreatureReady(player2, new ArchonOfJustice());

        harness.setHand(player1, List.of(new SpringjackShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).hasSize(1);
    }

    @Test
    @DisplayName("Counts your permanents when the ETB trigger resolves")
    void etbCountsAtTriggerResolution() {
        harness.setHand(player1, List.of(new SpringjackShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        addCreatureReady(player1, new ArchonOfJustice());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goat")).hasSize(3);
    }

    @Test
    @DisplayName("Devotion modifiers do not add extra Chroma symbols")
    void etbDoesNotCountDevotionModifiers() {
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        harness.setHand(player1, List.of(new SpringjackShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).hasSize(1);
    }

    @Test
    @DisplayName("Each white-black hybrid symbol contributes one Goat")
    void etbCountsHybridWhiteSymbols() {
        addCreatureReady(player1, new RestlessApparition());
        harness.setHand(player1, List.of(new SpringjackShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).hasSize(4);
    }

    @Test
    @DisplayName("An exiled Shepherd contributes no symbols, but its trigger still resolves")
    void etbResolvesAfterSourceLeavesWithNoWhitePermanents() {
        harness.setHand(player1, List.of(new SpringjackShepherd()));
        harness.setHand(player2, List.of(new Unmake()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0,
                findPermanent(player1, "Springjack Shepherd").getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Springjack Shepherd");
        assertThat(findPermanents(player1, "Goat")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
