package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HeliodSunCrowned;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.v.VoraciousTyphon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShatterTheSky.class, NyxbornColossus.class, NyxbornCourser.class, VoraciousTyphon.class})
class ShatterTheSkyTest extends BaseCardTest {

    @Test
    @DisplayName("Each player with a creature of power 4 or greater draws before all creatures are destroyed")
    void qualifyingPlayersDrawBeforeBoardWipe() {
        harness.addToBattlefield(player1, new NyxbornColossus());
        harness.addToBattlefield(player2, new NyxbornCourser());
        harness.setHand(player1, List.of(new ShatterTheSky()));
        harness.setHand(player2, List.of());
        int player1DeckSize = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckSize - 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckSize);
        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertNotOnBattlefield(player2, "Nyxborn Courser");
    }

    @Test
    @DisplayName("Does not draw for players whose creatures all have power less than 4")
    void nonqualifyingPlayersDoNotDraw() {
        harness.addToBattlefield(player1, new NyxbornCourser());
        harness.addToBattlefield(player2, new NyxbornCourser());
        harness.setHand(player1, List.of(new ShatterTheSky()));
        harness.setHand(player2, List.of());
        int player1DeckSize = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckSize);
        harness.assertNotOnBattlefield(player1, "Nyxborn Courser");
        harness.assertNotOnBattlefield(player2, "Nyxborn Courser");
    }
    @Test
    @DisplayName("Both players draw exactly one card even with multiple qualifying creatures")
    void bothPlayersDrawOnlyOneCard() {
        harness.addToBattlefield(player1, new VoraciousTyphon());
        harness.addToBattlefield(player1, new NyxbornColossus());
        harness.addToBattlefield(player2, new VoraciousTyphon());
        harness.setHand(player2, List.of());

        harness.castFromHand(player1, new ShatterTheSky(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Voracious Typhon");
        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        harness.assertInGraveyard(player2, "Voracious Typhon");
    }

    @Test
    @DisplayName("Checks modified power at resolution rather than printed power or casting time")
    void checksCurrentPowerAtResolution() {
        harness.addToBattlefield(player1, new NyxbornCourser());
        harness.addToBattlefield(player2, new VoraciousTyphon());
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new ShatterTheSky(), "{2}{W}{W}");

        gd.playerBattlefields.get(player1.getId()).getFirst()
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.playerBattlefields.get(player2.getId()).getFirst()
                .setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Nyxborn Courser");
        harness.assertInGraveyard(player2, "Voracious Typhon");
    }

    @Test
    @CardUsed(HeliodSunCrowned.class)
    @DisplayName("An indestructible qualifying creature draws and survives while a noncreature God does not draw")
    void respectsIndestructibleAndCurrentCreatureTypes() {
        harness.addToBattlefield(player1, new HeliodSunCrowned());
        harness.addToBattlefield(player1, new NyxbornCourser());
        harness.addToBattlefield(player1, new NyxbornCourser());
        harness.addToBattlefield(player2, new HeliodSunCrowned());
        harness.setHand(player2, List.of());

        harness.castFromHand(player1, new ShatterTheSky(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Heliod, Sun-Crowned");
        harness.assertOnBattlefield(player2, "Heliod, Sun-Crowned");
        harness.assertNotOnBattlefield(player1, "Nyxborn Courser");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Nyxborn Courser"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Resolves on an empty battlefield without either player drawing")
    void emptyBattlefieldDoesNotDraw() {
        harness.setHand(player2, List.of());

        harness.castFromHand(player1, new ShatterTheSky(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shatter the Sky");
    }
}
