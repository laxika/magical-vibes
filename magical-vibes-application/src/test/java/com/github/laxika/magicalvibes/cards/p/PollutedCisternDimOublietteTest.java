package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.BLACK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PollutedCisternDimOubliette.class, Forest.class, GloriousAnthem.class,
        GrizzlyBears.class, LightningBolt.class})
class PollutedCisternDimOublietteTest extends BaseCardTest {

    @Test
    void pollutedCisternMakesEachOpponentLoseForEachDistinctMilledCardType() {
        harness.setLibrary(player1, List.of(new Forest(), new LightningBolt(), new GloriousAnthem()));
        Permanent room = castRoom(0);
        harness.addMana(player1, BLACK, 5);

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void dimOublietteMillsThenReturnsACreatureFromTheGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castRoom(1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void pollutedCisternCountsRepeatedCardTypesOnlyOnce() {
        harness.setLibrary(player1, List.of(new PollutedCisternDimOubliette(),
                new PollutedCisternDimOubliette(), new PollutedCisternDimOubliette()));
        Permanent room = castRoom(0);
        harness.addMana(player1, BLACK, 5);

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void lockedPollutedCisternDoesNotTriggerWhenDimOublietteMills() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castRoom(1);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void dimOublietteCanReturnTheCreatureItJustMilled() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Forest()));

        castRoom(1);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void dimOublietteStillReturnsACreatureWhenTheLibraryIsEmpty() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castRoom(1);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void dimOublietteCannotDeclineReturningAnAvailableCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castRoom(1);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new PollutedCisternDimOubliette()));
        harness.addMana(player1, BLACK, doorIndex == 0 ? 2 : 5);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        if (doorIndex == 1) {
            harness.passBothPriorities();
        }
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().contains("Polluted Cistern"))
                .findFirst()
                .orElseThrow();
    }
}
