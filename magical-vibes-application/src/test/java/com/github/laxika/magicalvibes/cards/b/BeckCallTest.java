package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BeckCallTest extends BaseCardTest {

    private static final int BECK = 0;
    private static final int CALL = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Beck lets you draw when any creature enters")
    void beckDrawsForAnyCreatureEntering() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, BECK);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Call creates four flying Bird tokens")
    void callCreatesBirds() {
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, CALL);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .hasSize(4)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.BIRD);
                    assertThat(permanent.hasKeyword(Keyword.FLYING)).isTrue();
                    assertThat(permanent.getEffectivePower()).isEqualTo(1);
                    assertThat(permanent.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Fused Beck and Call triggers for each Bird token")
    void fusedTriggersForCallTokens() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, FUSE, List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }
}
