package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenSoulgazer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpittingGourna.class, AvenSoulgazer.class})
class SpittingGournaTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast face down and turned face up for its Morph cost")
    void canBeMorphedFaceDownAndTurnedFaceUp() {
        harness.setHand(player1, List.of(new SpittingGourna()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent gourna = findPermanent(player1, "Spitting Gourna");
        assertThat(gourna.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gourna));
        harness.passBothPriorities();

        assertThat(gourna.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Turning Spitting Gourna face up requires green mana")
    void turningFaceUpRequiresGreenMana() {
        harness.setHand(player1, List.of(new SpittingGourna()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent gourna = findPermanent(player1, "Spitting Gourna");
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(gourna)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gourna.isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("Reach allows Spitting Gourna to block a creature with flying")
    void reachAllowsBlockingFlyingCreature() {
        Permanent flyer = addCreatureReady(player1, new AvenSoulgazer());
        Permanent gourna = addCreatureReady(player2, new SpittingGourna());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(gourna),
                gd.playerBattlefields.get(player1.getId()).indexOf(flyer))));

        assertThat(gourna.isBlocking()).isTrue();
    }
}
