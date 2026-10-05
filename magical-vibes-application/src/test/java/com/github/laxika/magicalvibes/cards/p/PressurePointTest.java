package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PressurePoint.class, DukharaPeafowl.class, PropheticPrism.class})
class PressurePointTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature and draws a card")
    void tapsTargetCreatureAndDrawsCard() {
        harness.addToBattlefield(player2, new DukharaPeafowl());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new PressurePoint()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Dukhara Peafowl");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(findPermanent(player2, "Dukhara Peafowl").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Fizzles and does not draw when the target is removed before resolution")
    void fizzlesWhenTargetIsRemoved() {
        harness.addToBattlefield(player2, new DukharaPeafowl());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new PressurePoint()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Dukhara Peafowl");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        harness.assertInGraveyard(player1, "Pressure Point");
    }

    @Test
    @DisplayName("Draws a card even when the target creature is already tapped")
    void drawsWhenTargetIsAlreadyTapped() {
        var target = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());
        target.setTapped(true);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new PressurePoint()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        harness.assertInGraveyard(player1, "Pressure Point");
    }

    @Test
    @DisplayName("Can tap a creature controlled by the caster and draw a card")
    void canTargetOwnCreature() {
        var target = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new PressurePoint()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new PressurePoint()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Prophetic Prism");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
