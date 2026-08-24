package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KioraBehemothBeckoner.class, CrawWurm.class, Forest.class, HillGiant.class})
class KioraBehemothBeckonerTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a creature with power 4 or greater enters under your control")
    void drawsForOwnCreatureWithPowerAtLeastFour() {
        addReadyKiora(player1, 7);
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        int handSizeAfterCreatureEntry = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCreatureEntry + 1);
    }

    @Test
    @DisplayName("Does not draw for a creature with power less than 4")
    void doesNotDrawForSmallerCreature() {
        addReadyKiora(player1, 7);
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when an opponent's large creature enters")
    void doesNotDrawForOpponentsCreature() {
        addReadyKiora(player1, 7);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CrawWurm()));
        harness.addMana(player2, ManaColor.GREEN, 6);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("-1 untaps any target permanent")
    void minusOneUntapsTargetPermanent() {
        Permanent kiora = addReadyKiora(player1, 7);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(kiora.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("-1 cannot target a player")
    void minusOneRejectsPlayerTarget() {
        addReadyKiora(player1, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKiora(Player player, int loyalty) {
        Permanent kiora = harness.addToBattlefieldAndReturn(player, new KioraBehemothBeckoner());
        kiora.setCounterCount(CounterType.LOYALTY, loyalty);
        kiora.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return kiora;
    }
}
