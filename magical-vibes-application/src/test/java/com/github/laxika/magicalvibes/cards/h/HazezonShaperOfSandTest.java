package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({HazezonShaperOfSand.class, Desert.class, Forest.class})
class HazezonShaperOfSandTest extends BaseCardTest {

    @Test
    @DisplayName("Can play a Desert, but not another land, from the controller's graveyard")
    void onlyDesertsCanBePlayedFromGraveyard() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setGraveyard(player1, List.of(new Desert(), new Forest()));
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Desert"));
        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("A Desert entering creates two Sand Warrior tokens")
    void desertLandfallCreatesSandWarriors() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setHand(player1, List.of(new Desert()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = sandWarriorTokens();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("A non-Desert land does not create Sand Warrior tokens")
    void nonDesertLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new HazezonShaperOfSand());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(sandWarriorTokens()).isEmpty();
    }

    private List<Permanent> sandWarriorTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Sand Warrior"))
                .toList();
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
