package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TalrandSkySummoner;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrownInDreams.class, Forest.class, TalrandSkySummoner.class})
class DrownInDreamsTest extends BaseCardTest {

    @Test
    void targetPlayerDrawsXCards() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        cast(0, 2, List.of(player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void targetPlayerMillsTwiceXCards() {
        harness.setLibrary(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        cast(1, 2, List.of(player2.getId()));

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    void commanderAllowsBothModesAndSharedTarget() {
        var commander = new TalrandSkySummoner();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        harness.addToBattlefield(player1, commander);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        castModes(new int[]{0, 1}, 2,
                List.of(player2.getId(), player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    void bothModesRequireACommander() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        assertThatThrownBy(() -> castModes(
                new int[]{0, 1}, 2,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional modal modes are not available");
    }

    @Test
    void commanderInCommandZoneDoesNotAllowBothModes() {
        var commander = new TalrandSkySummoner();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        gd.playerCommandZones.get(player1.getId()).add(commander);

        assertThatThrownBy(() -> castModes(new int[]{0, 1}, 0,
                List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional modal modes are not available");
    }

    @Test
    void controllingOpponentsCommanderAllowsDifferentTargetsForBothModes() {
        var commander = new TalrandSkySummoner();
        gd.playerCommanders.put(player2.getId(), List.of(commander));
        harness.addToBattlefield(player1, commander);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        castModes(new int[]{0, 1}, 2, List.of(player1.getId(), player2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    void zeroXDrawsNoCards() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        cast(0, 0, List.of(player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void millingMoreThanLibrarySizeMillsOnlyAvailableCards() {
        harness.setLibrary(player2, List.of(new Forest()));

        cast(1, 2, List.of(player2.getId()));

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    private void cast(int mode, int xValue, List<java.util.UUID> targetIds) {
        castModes(new int[]{mode}, xValue, targetIds);
    }

    private void castModes(int[] modes, int xValue, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new DrownInDreams()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, xValue + 2);
        harness.castModalSorceryWithModesForX(player1, 0, 1, 2, modes, xValue, targetIds);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
