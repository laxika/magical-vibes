package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CanopyDragon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FearsomeWhelp.class, CanopyDragon.class, GrizzlyBears.class})
class FearsomeWhelpTest extends BaseCardTest {

    @Test
    void perpetuallyReducesDragonCardsInHandButNotOtherCards() {
        CanopyDragon dragonInHand = new CanopyDragon();
        harness.setHand(player1, List.of(dragonInHand, new GrizzlyBears()));
        harness.addToBattlefield(player1, new FearsomeWhelp());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), dragonInHand)).isEqualTo(-1);
        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), gd.playerHands.get(player1.getId()).get(1)))
                .isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }
}
