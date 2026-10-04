package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlacialStalker.class})
class GlacialStalkerTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new GlacialStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent stalker = findPermanent(player1, "Glacial Stalker");
        assertThat(stalker.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int stalkerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(stalker);
        harness.turnFaceUp(player1, stalkerIndex);
        harness.passBothPriorities();

        assertThat(stalker.isFaceDown()).isFalse();
    }

    @Test
    void cannotTurnFaceUpWithoutBlueMana() {
        Permanent stalker = castFaceDownStalker();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(stalker.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTurnFaceUpWithTooLittleGenericMana() {
        Permanent stalker = castFaceDownStalker();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(stalker.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turnsFaceUpImmediatelyWhileAnotherSpellIsOnTheStack() {
        Permanent stalker = castFaceDownStalker();
        harness.setHand(player1, List.of(new GlacialStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        var pendingSpell = gd.stack.getLast();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.turnFaceUp(player1, 0);

        assertThat(stalker.isFaceDown()).isFalse();
        assertThat(gd.stack).containsExactly(pendingSpell);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(stalker);
    }

    private Permanent castFaceDownStalker() {
        harness.setHand(player1, List.of(new GlacialStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent stalker = findPermanent(player1, "Glacial Stalker");
        assertThat(stalker.isFaceDown()).isTrue();
        return stalker;
    }
}
