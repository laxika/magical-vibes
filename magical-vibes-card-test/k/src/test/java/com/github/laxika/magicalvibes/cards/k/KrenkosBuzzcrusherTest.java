package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.e.EscapeTunnel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrenkosBuzzcrusher.class, EscapeTunnel.class, Forest.class, Island.class,
        CosisTrickster.class, AvenMindcensor.class})
class KrenkosBuzzcrusherTest extends BaseCardTest {

    @Test
    @DisplayName("The controller chooses one nonbasic land per player and destroyed lands are replaced")
    void choosesAndReplacesDestroyedLands() {
        Permanent ownChosen = harness.addToBattlefieldAndReturn(player1, new EscapeTunnel());
        Permanent ownSurvivor = harness.addToBattlefieldAndReturn(player1, new EscapeTunnel());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new EscapeTunnel());
        setLibrary(player1, new Forest());
        setLibrary(player2, new Island());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of(ownChosen.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentLand.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownSurvivor);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownChosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentLand);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest") && permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Island") && permanent.isTapped());
    }

    @Test
    @DisplayName("The up-to-one choices can all be declined")
    void canDeclineEveryLandChoice() {
        harness.addToBattlefield(player1, new EscapeTunnel());
        harness.addToBattlefield(player2, new EscapeTunnel());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Escape Tunnel"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Escape Tunnel"));
    }

    @Test
    void basicLandsAreNotOfferedForDestruction() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(island);
    }

    @Test
    @CardUsed(CosisTrickster.class)
    void decliningOptionalSearchDoesNotShuffle() {
        harness.addToBattlefield(player1, new CosisTrickster());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new EscapeTunnel());
        setLibrary(player2, new Island());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));
        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void playerCanDeclineSearchEvenWithoutBasicLandsInLibrary() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new EscapeTunnel());
        setLibrary(player2, new EscapeTunnel());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @CardUsed(AvenMindcensor.class)
    void laterPlayersSearchOnlyTopFourCardsWithMindcensor() {
        harness.addToBattlefield(player1, new AvenMindcensor());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new EscapeTunnel());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new EscapeTunnel());
        setLibrary(player1, new Forest());
        setLibrary(player2, new EscapeTunnel(), new EscapeTunnel(), new EscapeTunnel(),
                new EscapeTunnel(), new Island());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownLand.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentLand.getId()));
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        if (search != null) {
            assertThat(search.params().cards()).noneMatch(card -> card instanceof Island);
            harness.handleCardChosen(player2, -1);
        }
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Island);
    }

    private void cast() {
        harness.castFromHand(player1, new KrenkosBuzzcrusher(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Player player, Card... cards) {
        harness.setLibrary(player, List.of(cards));
    }
}
