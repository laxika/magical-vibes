package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CanyonLurkers.class})
class CanyonLurkersTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new CanyonLurkers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lurkers = findPermanent(player1, "Canyon Lurkers");
        assertThat(lurkers.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lurkersIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lurkers);
        harness.turnFaceUp(player1, lurkersIndex);
        harness.passBothPriorities();

        assertThat(lurkers.isFaceDown()).isFalse();
    }

    @Test
    void morphRequiresRedMana() {
        Permanent lurkers = addCreatureReady(player1, new CanyonLurkers());
        lurkers.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(lurkers.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void morphRequiresFullGenericPayment() {
        Permanent lurkers = addCreatureReady(player1, new CanyonLurkers());
        lurkers.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(lurkers.isFaceDown()).isTrue();
    }

    @Test
    void turningFaceUpIsImmediateAndPreservesThePermanent() {
        harness.setHand(player1, List.of(new CanyonLurkers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent lurkers = findPermanent(player1, "Canyon Lurkers");
        lurkers.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, 0);

        assertThat(lurkers.isFaceDown()).isFalse();
        assertThat(lurkers.isTapped()).isTrue();
        assertThat(lurkers.isSummoningSick()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(lurkers);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void faceDownLurkersTradeInCombat() {
        Permanent attacker = addCreatureReady(player1, new CanyonLurkers());
        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent blocker = addCreatureReady(player2, new CanyonLurkers());
        blocker.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Canyon Lurkers");
        harness.assertNotOnBattlefield(player2, "Canyon Lurkers");
        harness.assertInGraveyard(player1, "Canyon Lurkers");
        harness.assertInGraveyard(player2, "Canyon Lurkers");
    }
}
