package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OggyarBattleSeer.class})
class OggyarBattleSeerTest extends BaseCardTest {

    @Test
    void activatingAbilityTapsBattleSeerAndPutsAbilityOnStack() {
        Permanent seer = addCreatureReady(player1, new OggyarBattleSeer());

        harness.activateAbility(player1, 0, null, null);

        assertThat(seer.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void resolvingAbilityStartsScryOneInteraction() {
        addCreatureReady(player1, new OggyarBattleSeer());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1);
    }

    @Test
    void hasteAllowsTapAbilityImmediatelyAfterResolvingCreature() {
        harness.castFromHand(player1, new OggyarBattleSeer(), "{3}{U}{R}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void scryCanKeepTheTopCardWithoutChangingEitherLibrary() {
        addCreatureReady(player1, new OggyarBattleSeer());
        OggyarBattleSeer top = new OggyarBattleSeer();
        OggyarBattleSeer next = new OggyarBattleSeer();
        OggyarBattleSeer opponentTop = new OggyarBattleSeer();
        harness.setLibrary(player1, List.of(top, next));
        harness.setLibrary(player2, List.of(opponentTop));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void scryCanPutTheTopCardOnTheBottom() {
        addCreatureReady(player1, new OggyarBattleSeer());
        OggyarBattleSeer top = new OggyarBattleSeer();
        OggyarBattleSeer next = new OggyarBattleSeer();
        harness.setLibrary(player1, List.of(top, next));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void scryWithEmptyLibraryFinishesWithoutPromptingOrLosing() {
        addCreatureReady(player1, new OggyarBattleSeer());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new OggyarBattleSeer());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.stack).hasSize(1);
    }
}
