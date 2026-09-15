package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DartingMerfolk;
import com.github.laxika.magicalvibes.cards.d.DeepwoodWolverine;
import com.github.laxika.magicalvibes.cards.f.Ferocity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Revive.class, DeepwoodWolverine.class, DartingMerfolk.class, Ferocity.class})
class ReviveTest extends BaseCardTest {

    @Test
    @DisplayName("Revive returns target green card from graveyard to hand")
    void returnsTargetGreenCardFromGraveyardToHand() {
        Card green = new DeepwoodWolverine();
        harness.setGraveyard(player1, List.of(green));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, green.getId());

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(green.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(green.getId()));
    }

    @Test
    @DisplayName("Revive can return a green noncreature card from the graveyard")
    void returnsGreenNonCreatureCardFromGraveyardToHand() {
        Card green = new Ferocity();
        harness.setGraveyard(player1, List.of(green));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, green.getId());

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(green.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(green.getId()));
    }

    @Test
    @DisplayName("Revive cannot target a non-green card in graveyard")
    void cannotTargetNonGreenCard() {
        Card nonGreen = new DartingMerfolk();
        harness.setGraveyard(player1, List.of(nonGreen));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonGreen.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Revive cannot target a green card in opponent's graveyard")
    void cannotTargetCardInOpponentGraveyard() {
        Card green = new DeepwoodWolverine();
        harness.setGraveyard(player2, List.of(green));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, green.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Revive fizzles if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card green = new DeepwoodWolverine();
        harness.setGraveyard(player1, List.of(green));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, green.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(green.getId()));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Revive");
    }
}
