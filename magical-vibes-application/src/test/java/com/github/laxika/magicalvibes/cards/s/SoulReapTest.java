package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HobgoblinDragoon;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulReap.class, HobgoblinDragoon.class, NettleSentinel.class})
class SoulReapTest extends BaseCardTest {

    @Test
    @DisplayName("Without another black spell cast, the creature is destroyed but its controller keeps their life")
    void destroysCreatureNoLifeLossWithoutBlackSpell() {
        UUID target = addCreature(player2, new HobgoblinDragoon()); // red-white, nongreen

        castSoulReap(target);

        harness.assertInGraveyard(player2, "Hobgoblin Dragoon");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("After another black spell this turn, the destroyed creature's controller loses 3 life")
    void controllerLosesLifeAfterBlackSpell() {
        UUID first = addCreature(player2, new HobgoblinDragoon()); // red-white, nongreen

        // First Soul Reap is the "another black spell" cast this turn — no life loss yet.
        castSoulReap(first);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        // Second Soul Reap sees a prior black spell, so its controller loses 3 life.
        UUID second = addCreature(player2, new HobgoblinDragoon());
        castSoulReap(second);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A prior non-black spell does not trigger the life loss")
    void priorNonBlackSpellDoesNotTriggerLifeLoss() {
        UUID target = addCreature(player2, new HobgoblinDragoon());

        // Casting a green creature is a spell, but not a black one.
        harness.setHand(player1, List.of(new NettleSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        castSoulReap(target);

        harness.assertInGraveyard(player2, "Hobgoblin Dragoon");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a green creature")
    void cannotTargetGreenCreature() {
        UUID green = addCreature(player2, new NettleSentinel());
        harness.setHand(player1, List.of(new SoulReap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(green)))
                .isInstanceOf(IllegalStateException.class);
    }

    private UUID addCreature(Player owner, Card card) {
        return harness.addToBattlefieldAndReturn(owner, card).getId();
    }

    private void castSoulReap(UUID targetId) {
        harness.setHand(player1, List.of(new SoulReap()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));
    }
}
