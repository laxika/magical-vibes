package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImpetuousLootmonger.class, Divination.class, Forest.class, GrizzlyBears.class, Island.class})
class ImpetuousLootmongerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB discards a card, then heists three random nonland cards from the target opponent")
    void enteringDiscardsThenHeistsTargetOpponentsLibrary() {
        Card discarded = new Forest();
        harness.setHand(player1, List.of(new ImpetuousLootmonger(), discarded));
        harness.setLibrary(player2, List.of(
                new Forest(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Island()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice.playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, discardChoice.validIndices().getFirst());

        PendingInteraction.LibrarySearch heistChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(heistChoice).isNotNull();
        assertThat(heistChoice.params().cards()).hasSize(3)
                .allMatch(card -> !card.hasType(CardType.LAND));

        Card chosen = heistChoice.params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isTrue();
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(chosen.getId());
    }

    @Test
    @DisplayName("Casting a spell you do not own creates a tapped Treasure token")
    void castingUnownedSpellCreatesTappedTreasure() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new ImpetuousLootmonger());
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        Divination spell = new Divination();
        spell.setOwnerId(player1.getId());
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        Permanent treasure = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Treasure"))
                .findFirst()
                .orElseThrow();
        assertThat(treasure.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a spell you own does not create a Treasure token")
    void castingOwnedSpellCreatesNoTreasure() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ImpetuousLootmonger());
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        Divination spell = new Divination();
        spell.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Treasure");
    }
}
