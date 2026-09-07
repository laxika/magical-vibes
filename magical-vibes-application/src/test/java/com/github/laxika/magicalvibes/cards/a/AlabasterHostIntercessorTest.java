package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlabasterHostIntercessor.class, GrizzlyBears.class, Plains.class, Forest.class, Unsummon.class})
class AlabasterHostIntercessorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles target creature an opponent controls")
    void etbExilesOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castIntercessor(bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled creature returns when Alabaster Host Intercessor leaves the battlefield")
    void exiledCreatureReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castIntercessor(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID intercessorId = harness.getPermanentId(player1, "Alabaster Host Intercessor");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, intercessorId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new AlabasterHostIntercessor()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Plainscycling discards the card and offers only Plains cards")
    void plainscyclingSearchesForPlains() {
        harness.setHand(player1, List.of(new AlabasterHostIntercessor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new GrizzlyBears()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alabaster Host Intercessor");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).hasSize(1).allMatch(card -> card.getName().equals("Plains"));

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Plains");
    }

    private void castIntercessor(UUID targetId) {
        harness.setHand(player1, List.of(new AlabasterHostIntercessor()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
