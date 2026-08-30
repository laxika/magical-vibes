package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlamescrollCelebrantTest extends BaseCardTest {

    @Test
    void dealsDamageWhenOpponentActivatesNonManaAbility() {
        addCreatureReady(player1, new FlamescrollCelebrant());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.setLife(player2, 20);
        forceMainPhase(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void activatedAbilityBoostsPowerUntilEndOfTurn() {
        Permanent celebrant = addCreatureReady(player1, new FlamescrollCelebrant());
        int powerBefore = gqs.getEffectivePower(gd, celebrant);
        forceMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, celebrant)).isEqualTo(powerBefore + 2);
    }

    @Test
    void revelInSilenceStopsOpponentsCastingAndActivatingLoyaltyAbilities() {
        Permanent chandra = new Permanent(new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        chandra.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(chandra);
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.setHand(player1, List.of(new FlamescrollCelebrant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playersSilencedThisTurn).contains(player2.getId());
        assertThat(gd.playersCantActivatePlaneswalkerLoyaltyAbilitiesThisTurn)
                .contains(player2.getId())
                .doesNotContain(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Flamescroll Celebrant");

        forceMainPhase(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player2, 1, null, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    private void forceMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
