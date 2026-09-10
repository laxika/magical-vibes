package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BayouDragonfly;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturalSpring.class, BayouDragonfly.class})
class NaturalSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Natural Spring targeting a player puts it on the stack")
    void castingTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new NaturalSpring()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast Natural Spring without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new NaturalSpring()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Natural Spring gains 8 life for target player")
    void gains8LifeForTargetPlayer() {
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new NaturalSpring()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Natural Spring can target yourself")
    void canTargetSelf() {
        harness.setLife(player1, 7);
        harness.setHand(player1, List.of(new NaturalSpring()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Natural Spring cannot target a creature")
    void cannotTargetCreature() {
        Permanent dragonfly = harness.addToBattlefieldAndReturn(player2, new BayouDragonfly());

        harness.setHand(player1, List.of(new NaturalSpring()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, dragonfly.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Natural Spring goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        NaturalSpring card = new NaturalSpring();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(graveyardCard -> graveyardCard.getId().equals(card.getId()));
    }
}
