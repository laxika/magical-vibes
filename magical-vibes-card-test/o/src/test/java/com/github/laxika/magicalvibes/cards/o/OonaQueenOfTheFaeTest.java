package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OonaQueenOfTheFae.class, Forest.class, GrizzlyBears.class, Peek.class, DryadArbor.class})
class OonaQueenOfTheFaeTest extends BaseCardTest {

    private void setupOona() {
        addCreatureReady(player1, new OonaQueenOfTheFae());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 8);
    }

    private long faerieRogueCount() {
        return countPermanents(player1, "Faerie Rogue");
    }

    @Test
    @DisplayName("Resolving the ability awaits the controller's color choice")
    void resolvingAwaitsColorChoice() {
        setupOona();
        harness.setLibrary(player2, List.of(new Peek(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Creates one Faerie Rogue per exiled card of the chosen color")
    void createsTokenPerChosenColorCard() {
        setupOona();
        harness.setLibrary(player2, List.of(new Peek(), new Peek(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        // Top three exiled; the two blue Peeks each make a Faerie Rogue, the green Grizzly Bears does not.
        assertThat(faerieRogueCount()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lands are never counted as being of the chosen color")
    void landsAreNotCounted() {
        setupOona();
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        // Grizzly Bears (green) makes a token; Forest is a colorless land and is excluded.
        assertThat(faerieRogueCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a color none of the exiled cards are creates no tokens")
    void noMatchesCreatesNoTokens() {
        setupOona();
        harness.setLibrary(player2, List.of(new Peek(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(faerieRogueCount()).isZero();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target the controller (must target an opponent)")
    void cannotTargetSelf() {
        setupOona();
        harness.setLibrary(player2, List.of(new Peek()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A green land card counts when green is chosen")
    void coloredLandCreatesToken() {
        setupOona();
        DryadArbor arbor = new DryadArbor();
        harness.setLibrary(player2, List.of(arbor));

        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(arbor);
        assertThat(faerieRogueCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("X zero still chooses a color and leaves the library unchanged")
    void zeroExilesNothing() {
        setupOona();
        Peek peek = new Peek();
        harness.setLibrary(player2, List.of(peek));

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(peek);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(faerieRogueCount()).isZero();
    }

    @Test
    @DisplayName("X larger than the library counts only cards actually exiled")
    void shortLibraryExilesAvailableCards() {
        setupOona();
        Peek peek = new Peek();
        harness.setLibrary(player2, List.of(peek));

        harness.activateAbility(player1, 0, 5, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(peek);
        assertThat(faerieRogueCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("A multicolored card creates one token with the specified characteristics")
    void multicoloredCardCreatesOneToken() {
        setupOona();
        harness.setLibrary(player2, List.of(new OonaQueenOfTheFae()));

        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(findPermanents(player1, "Faerie Rogue")).singleElement().satisfies(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveColors(gd, token)).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.FAERIE, CardSubtype.ROGUE);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        });
        assertThat(countPermanents(player2, "Faerie Rogue")).isZero();
    }

    @Test
    @DisplayName("The activated ability resolves after Oona leaves the battlefield")
    void resolvesWithoutSource() {
        setupOona();
        harness.setLibrary(player2, List.of(new OonaQueenOfTheFae()));

        harness.activateAbility(player1, 0, 1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(faerieRogueCount()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The hybrid activation cost can be paid with black mana")
    void activationAcceptsBlackMana() {
        addCreatureReady(player1, new OonaQueenOfTheFae());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLibrary(player2, List.of(new OonaQueenOfTheFae()));

        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(faerieRogueCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("An empty library still permits choosing a color and creates no tokens")
    void emptyLibraryCreatesNoTokens() {
        setupOona();
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(faerieRogueCount()).isZero();
    }
}
