package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.p.Pendelhaven;
import com.github.laxika.magicalvibes.cards.w.WindsOfChange;
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

@CardUsed({FlashCounter.class, Boomerang.class, Pendelhaven.class, WindsOfChange.class})
class FlashCounterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting an instant spell")
    void castingTargetsInstantSpell() {
        Permanent pendelhaven = harness.addToBattlefieldAndReturn(player1, new Pendelhaven());
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, pendelhaven.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        assertThat(gd.stack).hasSize(2);
        StackEntry flashCounterEntry = gd.stack.getLast();
        assertThat(flashCounterEntry.getTargetId()).isEqualTo(boomerang.getId());
    }

    @Test
    @DisplayName("Resolving counters the instant spell")
    void countersInstantSpell() {
        Permanent pendelhaven = harness.addToBattlefieldAndReturn(player1, new Pendelhaven());
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, pendelhaven.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boomerang");
        harness.assertOnBattlefield(player1, "Pendelhaven");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if the target instant spell is no longer on the stack")
    void fizzlesIfTargetInstantSpellRemoved() {
        Permanent pendelhaven = harness.addToBattlefieldAndReturn(player1, new Pendelhaven());
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        FlashCounter flashCounter = new FlashCounter();
        harness.setHand(player2, List.of(flashCounter));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, pendelhaven.getId());
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
        WindsOfChange windsOfChange = new WindsOfChange();
        harness.setHand(player1, List.of(windsOfChange));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new FlashCounter()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, windsOfChange.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
