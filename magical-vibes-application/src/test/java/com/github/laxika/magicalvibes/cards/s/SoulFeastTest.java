package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanyonWildcat;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulFeast.class, CanyonWildcat.class})
class SoulFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Soul Feast targeting a player puts it on the stack")
    void castingTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new SoulFeast()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(SoulFeast.class);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Soul Feast causes target player to lose 4 life and controller to gain 4 life")
    void drainsFourAndGainsFour() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SoulFeast()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Soul Feast can target yourself")
    void canTargetSelf() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new SoulFeast()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Soul Feast resolves both effects when its controller targets themself at low life")
    void targetingSelfAtLowLifeResolvesBothEffects() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new SoulFeast()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 3);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Soul Feast gains life before state-based actions check a target below zero life")
    void gainsLifeBeforeTargetLosesGame() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 3);
        harness.setHand(player1, List.of(new SoulFeast()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 24);
        harness.assertLife(player2, -1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Soul Feast cannot target a creature")
    void cannotTargetCreature() {
        Permanent cat = harness.addToBattlefieldAndReturn(player2, new CanyonWildcat());

        harness.setHand(player1, List.of(new SoulFeast()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, cat.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Soul Feast goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new SoulFeast()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Soul Feast");
    }
}
