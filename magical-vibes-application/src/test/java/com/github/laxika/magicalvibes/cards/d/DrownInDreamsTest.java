package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrownInDreams.class, Forest.class, GrizzlyBears.class})
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
        gd.playerCommandZones.get(player1.getId()).add(new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        castEncoded(ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}), 2,
                List.of(player2.getId(), player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    void bothModesRequireACommander() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        assertThatThrownBy(() -> castEncoded(
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}), 2,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional modal modes are not available");
    }

    private void cast(int mode, int xValue, List<java.util.UUID> targetIds) {
        castEncoded(ChooseOneEffect.encodeModeSelection(1, 2, new int[]{mode}), xValue, targetIds);
    }

    private void castEncoded(int mode, int xValue, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new DrownInDreams()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, xValue + 2);
        gs.playModalXCard(gd, player1, 0, mode, xValue, null, targetIds);
        harness.passBothPriorities();
    }
}
