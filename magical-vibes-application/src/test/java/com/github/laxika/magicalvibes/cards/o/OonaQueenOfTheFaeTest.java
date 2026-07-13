package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OonaQueenOfTheFaeTest extends BaseCardTest {

    private void setupOona() {
        addCreatureReady(player1, new OonaQueenOfTheFae());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 8);
    }

    private long faerieRogueCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Faerie Rogue"))
                .count();
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
}
