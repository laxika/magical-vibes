package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({RainOfTears.class, LowlandGiant.class, Mountain.class})
class RainOfTearsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rain of Tears puts it on the stack with target")
    void castingPutsOnStack() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, mountain.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(mountain.getId());
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvingDestroysTargetLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, mountain.getId());

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Can destroy own land")
    void canDestroyOwnLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, mountain.getId());

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Rain of Tears goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, mountain.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rain of Tears");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, mountain.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Rain of Tears");
    }

    @Test
    @DisplayName("Cannot destroy a creature with Rain of Tears")
    void cannotDestroyCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LowlandGiant());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
