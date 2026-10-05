package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PonybackBrigade.class})
class PonybackBrigadeTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldCreatesThreeGoblins() {
        harness.castFromHand(player1, new PonybackBrigade(), "{3}{R}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).hasSize(3)
                .allSatisfy(goblin -> {
                    assertThat(goblin.getEffectivePower()).isEqualTo(1);
                    assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
                    assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
                    assertThat(goblin.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
                });
    }

    @Test
    void turningFaceUpCreatesThreeGoblins() {
        harness.setHand(player1, List.of(new PonybackBrigade()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent brigade = findPermanent(player1, "Ponyback Brigade");
        assertThat(brigade.isFaceDown()).isTrue();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(brigade));
        harness.passBothPriorities();

        assertThat(brigade.isFaceDown()).isFalse();
        assertThat(findPermanents(player1, "Goblin")).hasSize(3);
    }

    @Test
    void missingBlackManaPreventsTurningFaceUpAndCreatingTokens() {
        harness.setHand(player1, List.of(new PonybackBrigade()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent brigade = findPermanent(player1, "Ponyback Brigade");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(brigade)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(brigade.isFaceDown()).isTrue();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringUnderSecondPlayersControlCreatesTheirTokens() {
        gd.activePlayerId = player2.getId();
        harness.castFromHand(player2, new PonybackBrigade(), "{3}{R}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Goblin")).hasSize(3);
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }
}
