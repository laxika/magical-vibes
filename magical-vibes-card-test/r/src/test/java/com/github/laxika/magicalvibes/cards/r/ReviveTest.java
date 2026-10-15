package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlanchwoodArmor;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Revive.class, GrizzlyBears.class, GiantGrowth.class, HillGiant.class, BlanchwoodArmor.class})
class ReviveTest extends BaseCardTest {

    @Test
    @DisplayName("Revive returns target green card from graveyard to hand")
    void returnsTargetGreenCardFromGraveyardToHand() {
        Card green = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(green));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, green.getId());

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(green.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(green.getId()));
    }

    @Test
    @DisplayName("Revive returns a target green noncreature card from graveyard to hand")
    void returnsTargetGreenNoncreatureCardFromGraveyardToHand() {
        Card green = new GiantGrowth();
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
        Card red = new HillGiant();
        harness.setGraveyard(player1, List.of(red));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, red.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Revive cannot target a green card in opponent's graveyard")
    void cannotTargetCardInOpponentGraveyard() {
        Card green = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(green));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, green.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Revive can return a green noncreature card from the graveyard")
    void returnsGreenNonCreatureCardFromGraveyardToHand() {
        Card green = new BlanchwoodArmor();
        harness.setGraveyard(player1, List.of(green));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, green.getId());

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(green.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(green.getId()));
    }

    @Test
    @DisplayName("Revive fizzles if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card green = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(green));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, green.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(green.getId()));
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Revive");
    }

    @Test
    @DisplayName("Revive returns only the selected card from a mixed graveyard")
    void returnsOnlySelectedCard() {
        Card selected = new GrizzlyBears();
        Card otherGreen = new GiantGrowth();
        Card red = new HillGiant();
        harness.setGraveyard(player1, List.of(selected, otherGreen, red));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, selected.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherGreen, red);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(selected);
        harness.assertInGraveyard(player1, "Revive");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Revive cannot be cast without a target even with a green card available")
    void cannotCastWithoutTarget() {
        Card green = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(green));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
