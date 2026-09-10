package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildWurm.class, DarkBanishing.class})
class WildWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield triggers the coin flip ability")
    void entersTriggersCoinFlip() {
        WildWurm wildWurm = new WildWurm();
        harness.castFromHand(player1, wildWurm, "{3}{R}");

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(wildWurm);
        assertThat(gameLogContains("coin flip")).isFalse();
    }

    @Test
    @DisplayName("Resolution flips a coin and Wild Wurm ends in exactly one legal zone")
    void resolutionFlipsCoinAndMovesOrStays() {
        WildWurm wildWurm = new WildWurm();
        harness.castFromHand(player1, wildWurm, "{3}{R}");

        resolveAllTriggers();

        boolean onBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard() == wildWurm);
        boolean inHand = gd.playerHands.get(player1.getId()).stream()
                .anyMatch(c -> c == wildWurm);

        assertThat(onBattlefield != inHand).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c == wildWurm);
        if (inHand) {
            assertThat(gameLogContains("returned to its owner's hand")).isTrue();
        }

        assertThat(gameLogContains("coin flip")).isTrue();
    }

    @Test
    @DisplayName("If it leaves before its trigger resolves, the creature is not returned from the graveyard")
    void doesNotReturnIfItLeavesBeforeTriggerResolves() {
        WildWurm wildWurm = new WildWurm();
        harness.castFromHand(player1, wildWurm, "{3}{R}");
        harness.passBothPriorities();

        Permanent wildWurmPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == wildWurm)
                .findFirst()
                .orElseThrow();

        DarkBanishing darkBanishing = new DarkBanishing();
        harness.setHand(player2, List.of(darkBanishing));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, wildWurmPermanent.getId());

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() == wildWurm);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c == wildWurm);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c == wildWurm);
        assertThat(gameLogContains("coin flip")).isTrue();
    }
}
