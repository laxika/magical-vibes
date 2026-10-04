package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Eyekite.class, SnowCoveredIsland.class})
class EyekiteTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 after its controller draws two cards")
    void gainsPowerAfterControllerDrawsTwoCards() {
        Permanent eyekite = harness.addToBattlefieldAndReturn(player1, new Eyekite());
        harness.setLibrary(player1, List.of(new SnowCoveredIsland(), new SnowCoveredIsland()));

        assertThat(gqs.getEffectivePower(gd, eyekite)).isEqualTo(1);

        draw(player1);
        assertThat(gqs.getEffectivePower(gd, eyekite)).isEqualTo(1);

        draw(player1);
        assertThat(gqs.getEffectivePower(gd, eyekite)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's draws do not grant the power bonus")
    void opponentDrawsDoNotGrantPowerBonus() {
        Permanent eyekite = harness.addToBattlefieldAndReturn(player1, new Eyekite());
        harness.setLibrary(player2, List.of(new SnowCoveredIsland(), new SnowCoveredIsland()));

        draw(player2);
        draw(player2);

        assertThat(gqs.getEffectivePower(gd, eyekite)).isEqualTo(1);
    }

    @Test
    @DisplayName("More than two draws grant only one +2/+0 bonus")
    void additionalDrawsDoNotStackBonus() {
        Permanent eyekite = harness.addToBattlefieldAndReturn(player1, new Eyekite());
        harness.setLibrary(player1, List.of(new SnowCoveredIsland(), new SnowCoveredIsland(), new SnowCoveredIsland()));

        draw(player1);
        draw(player1);
        draw(player1);

        assertThat(gqs.getEffectivePower(gd, eyekite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eyekite)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draws before Eyekite enters count toward the bonus")
    void drawsBeforeEnteringCount() {
        harness.setLibrary(player1, List.of(new SnowCoveredIsland(), new SnowCoveredIsland()));
        draw(player1);
        draw(player1);

        Permanent eyekite = harness.enterBattlefieldAndReturn(player1, new Eyekite());

        assertThat(gqs.getEffectivePower(gd, eyekite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eyekite)).isEqualTo(2);
    }

    @Test
    @DisplayName("The bonus resets next turn and can apply on an opponent's turn")
    void bonusResetsAndCanApplyDuringOpponentsTurn() {
        Permanent eyekite = harness.addToBattlefieldAndReturn(player1, new Eyekite());
        harness.setLibrary(player1, List.of(new SnowCoveredIsland(), new SnowCoveredIsland(),
                new SnowCoveredIsland(), new SnowCoveredIsland()));
        harness.setLibrary(player2, List.of(new SnowCoveredIsland(), new SnowCoveredIsland()));
        draw(player1);
        draw(player1);
        assertThat(gqs.getEffectivePower(gd, eyekite)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, eyekite)).isEqualTo(1);

        draw(player1);
        assertThat(gqs.getEffectivePower(gd, eyekite)).isEqualTo(1);
        draw(player1);
        assertThat(gqs.getEffectivePower(gd, eyekite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eyekite)).isEqualTo(2);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
