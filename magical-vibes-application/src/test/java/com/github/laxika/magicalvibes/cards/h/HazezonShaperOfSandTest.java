package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.d.DesertOfTheFervent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HazezonShaperOfSand.class, Desert.class, DesertOfTheFervent.class, Forest.class})
class HazezonShaperOfSandTest extends BaseCardTest {

    @Test
    @DisplayName("The DMC Desert card can also be played from the graveyard")
    void canPlayDesertCardFromGraveyard() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setGraveyard(player1, List.of(new Desert()));
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Desert");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can play a Desert from the controller's graveyard")
    void canPlayDesertFromGraveyard() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setGraveyard(player1, List.of(new DesertOfTheFervent()));
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Desert of the Fervent");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot play a non-Desert land from the controller's graveyard")
    void cannotPlayNonDesertFromGraveyard() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("A Desert entering under your control creates two multicolored Sand Warriors")
    void desertLandfallCreatesSandWarriors() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setHand(player1, List.of(new DesertOfTheFervent()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = sandWarriorTokens(player1);
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN, CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
        }
    }

    @Test
    @DisplayName("A non-Desert land does not create Sand Warriors")
    void nonDesertLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(sandWarriorTokens(player1)).isEmpty();
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private List<Permanent> sandWarriorTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Sand Warrior"))
                .toList();
    }
}
