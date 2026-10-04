package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.d.DaringApprentice;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlashCounter.class, Boomerang.class, Concentrate.class, DaringApprentice.class,
        Island.class})
class FlashCounterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting an instant spell")
    void castingTargetsInstantSpell() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, island.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        assertThat(gd.stack).hasSize(2);
        StackEntry flashCounterEntry = gd.stack.getLast();
        assertThat(flashCounterEntry.getTargetId()).isEqualTo(boomerang.getId());
    }

    @Test
    @DisplayName("Resolving counters the instant spell")
    void countersInstantSpell() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, island.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, boomerang.getId());

        harness.assertInGraveyard(player1, "Boomerang");
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if the target instant spell is no longer on the stack")
    void fizzlesIfTargetInstantSpellRemoved() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        FlashCounter flashCounter = new FlashCounter();
        harness.setHand(player2, List.of(flashCounter));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, island.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        gd.stack.removeIf(stackEntry -> stackEntry.getCard().getId().equals(boomerang.getId()));

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player2, "Flash Counter");
    }

    @Test
    @DisplayName("Cannot target a non-instant spell")
    void cannotTargetNonInstantSpell() {
        Concentrate concentrate = new Concentrate();
        harness.castFromHand(player1, concentrate, "{2}{U}{U}");

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, concentrate.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability")
    void cannotTargetActivatedAbility() {
        DaringApprentice apprentice = new DaringApprentice();
        addCreatureReady(player1, apprentice);

        Concentrate concentrate = new Concentrate();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, concentrate, "{2}{U}{U}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, concentrate.getId());

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, apprentice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can counter an instant controlled by its own controller")
    void countersOwnInstant() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang, new FlashCounter()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, island.getId());
        harness.castAndResolveInstant(player1, 0, boomerang.getId());

        harness.assertInGraveyard(player1, "Boomerang");
        harness.assertInGraveyard(player1, "Flash Counter");
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without a target instant spell")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new FlashCounter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Flash Counter");
        assertThat(gd.stack).isEmpty();
    }
}
