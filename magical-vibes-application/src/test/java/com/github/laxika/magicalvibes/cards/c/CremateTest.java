package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cremate.class, RazorfootGriffin.class})
class CremateTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target card from an opponent's graveyard and draws a card")
    void exilesCardAndDraws() {
        Card griffin = new RazorfootGriffin();
        harness.setGraveyard(player2, List.of(griffin));
        harness.setHand(player1, List.of(new Cremate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, griffin.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player2, "Razorfoot Griffin");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Razorfoot Griffin"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Can exile a card from own graveyard")
    void exilesFromOwnGraveyard() {
        Card griffin = new RazorfootGriffin();
        harness.setGraveyard(player1, List.of(griffin));
        harness.setHand(player1, List.of(new Cremate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, griffin.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Razorfoot Griffin"));
    }

    @Test
    @DisplayName("Can exile a noncreature card from a graveyard")
    void exilesNoncreatureCard() {
        Card target = new Cremate();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new Cremate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player2, "Cremate");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Fizzles and does not draw if the target leaves before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        Card griffin = new RazorfootGriffin();
        harness.setGraveyard(player2, List.of(griffin));
        harness.setHand(player1, List.of(new Cremate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, griffin.getId());
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(griffin.getId()));
    }

    @Test
    @DisplayName("Cannot cast without a graveyard target")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new Cremate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
