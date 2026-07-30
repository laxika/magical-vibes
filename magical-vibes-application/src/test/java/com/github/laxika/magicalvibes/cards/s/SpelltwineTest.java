package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpelltwineTest extends BaseCardTest {

    private void addSpelltwineMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    @DisplayName("Casts a copy of a card from each graveyard and exiles both originals")
    void castsCopiesOfBothTargets() {
        CounselOfTheSoratami ownCounsel = new CounselOfTheSoratami();
        CounselOfTheSoratami opponentCounsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of(opponentCounsel));

        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castSorcery(player1, 0, List.of(ownCounsel.getId(), opponentCounsel.getId()));
        harness.passBothPriorities(); // resolve Spelltwine → both copies go on the stack
        harness.passBothPriorities(); // resolve the first copy
        harness.passBothPriorities(); // resolve the second copy

        // Spelltwine cast the Spelltwine card out of hand, so both copies drew two cards each.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + 4);

        // Both originals were exiled, and neither graveyard got a copy put into it.
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(exiledNames(player1)).contains("Counsel of the Soratami", "Spelltwine");
        assertThat(exiledNames(player2)).contains("Counsel of the Soratami");
    }

    @Test
    @DisplayName("A copied card that needs a target prompts for one")
    void copyOfTargetedCardPromptsForTarget() {
        CounselOfTheSoratami ownCounsel = new CounselOfTheSoratami();
        Shock opponentShock = new Shock();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of(opponentShock));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        harness.castSorcery(player1, 0, List.of(ownCounsel.getId(), opponentShock.getId()));
        harness.passBothPriorities(); // resolve Spelltwine → the Shock copy asks for a target

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both targets may not come from the same graveyard")
    void rejectsTwoTargetsFromOwnGraveyard() {
        CounselOfTheSoratami first = new CounselOfTheSoratami();
        CounselOfTheSoratami second = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(first, second));

        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId(), second.getId())))
                .hasMessageContaining("opponent's graveyard");
    }

    @Test
    @DisplayName("A creature card in a graveyard is not a legal target")
    void rejectsCreatureCardTarget() {
        CounselOfTheSoratami ownCounsel = new CounselOfTheSoratami();
        GrizzlyBears opponentBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of(opponentBears));

        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(ownCounsel.getId(), opponentBears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that leaves its graveyard before resolution is simply skipped")
    void skipsTargetThatLeftGraveyard() {
        CounselOfTheSoratami ownCounsel = new CounselOfTheSoratami();
        CounselOfTheSoratami opponentCounsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of(opponentCounsel));

        harness.setHand(player1, List.of(new Spelltwine()));
        addSpelltwineMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castSorcery(player1, 0, List.of(ownCounsel.getId(), opponentCounsel.getId()));
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities(); // resolve Spelltwine → only the surviving copy is cast
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + 2);
    }

    private List<String> exiledNames(com.github.laxika.magicalvibes.model.Player player) {
        return gd.getPlayerExiledCards(player.getId()).stream().map(Card::getName).toList();
    }
}
