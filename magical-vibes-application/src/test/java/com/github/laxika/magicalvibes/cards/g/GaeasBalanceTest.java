package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaeasBalance.class, Forest.class, GrizzlyBears.class, Island.class, Mountain.class,
        Plains.class, Swamp.class})
class GaeasBalanceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices exactly five lands")
    void castingSacrificesFiveLands() {
        List<Card> sacrificedCards = List.of(new Plains(), new Island(), new Swamp(), new Mountain(), new Forest());
        List<UUID> sacrificeIds = addLandsToBattlefield(sacrificedCards);
        harness.setHand(player1, List.of(new GaeasBalance()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorceryWithSacrifices(player1, 0, null, sacrificeIds);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(sacrificedCards);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot pay the additional cost with fewer than five lands")
    void cannotCastWithoutFiveLands() {
        List<Card> sacrificeCards = List.of(new Plains(), new Island(), new Swamp(), new Mountain());
        List<UUID> sacrificeIds = addLandsToBattlefield(sacrificeCards);
        harness.setHand(player1, List.of(new GaeasBalance()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, null, sacrificeIds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Searches for a land of each basic land type and puts the lands onto the battlefield")
    void searchesForEachBasicLandType() {
        List<Card> sacrificedCards = List.of(new Plains(), new Island(), new Swamp(), new Mountain(), new Forest());
        List<UUID> sacrificeIds = addLandsToBattlefield(sacrificedCards);
        List<Card> searchedLands = List.of(new Plains(), new Island(), new Swamp(), new Mountain(), new Forest());
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, new ArrayList<>(List.of(
                searchedLands.get(0), nonland, searchedLands.get(1), searchedLands.get(2), searchedLands.get(3),
                searchedLands.get(4))));
        harness.setHand(player1, List.of(new GaeasBalance()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorceryWithSacrifices(player1, 0, null, sacrificeIds);
        harness.passBothPriorities();

        for (Card searchedLand : searchedLands) {
            PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search).isNotNull();
            assertThat(search.params().cards()).containsExactly(searchedLand);
            harness.getGameService().handleInteractionAnswer(
                    gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().map(Permanent::getCard))
                .containsExactlyInAnyOrderElementsOf(searchedLands);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
    }

    private List<UUID> addLandsToBattlefield(List<Card> lands) {
        List<UUID> ids = new ArrayList<>();
        for (Card land : lands) {
            ids.add(harness.addToBattlefieldAndReturn(player1, land).getId());
        }
        return ids;
    }
}
