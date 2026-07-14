package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulReapTest extends BaseCardTest {

    @Test
    @DisplayName("Without another black spell cast, the creature is destroyed but its controller keeps their life")
    void destroysCreatureNoLifeLossWithoutBlackSpell() {
        UUID target = addCreature(player2, new HillGiant()); // red, nongreen

        castSoulReap(target);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("After another black spell this turn, the destroyed creature's controller loses 3 life")
    void controllerLosesLifeAfterBlackSpell() {
        UUID first = addCreature(player2, new HillGiant()); // red, nongreen
        UUID second = addCreature(player2, new EliteVanguard()); // white, nongreen

        // First Soul Reap is the "another black spell" cast this turn — no life loss yet.
        castSoulReap(first);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        // Second Soul Reap sees a prior black spell, so its controller loses 3 life.
        castSoulReap(second);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A prior non-black spell does not trigger the life loss")
    void priorNonBlackSpellDoesNotTriggerLifeLoss() {
        UUID target = addCreature(player2, new HillGiant());

        // Casting a green creature is a spell, but not a black one.
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        castSoulReap(target);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a green creature")
    void cannotTargetGreenCreature() {
        UUID green = addCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulReap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(green)))
                .isInstanceOf(IllegalStateException.class);
    }

    private UUID addCreature(Player owner, Card card) {
        harness.addToBattlefield(owner, card);
        return harness.getPermanentId(owner, card.getName());
    }

    private void castSoulReap(UUID targetId) {
        harness.setHand(player1, List.of(new SoulReap()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();
    }
}
