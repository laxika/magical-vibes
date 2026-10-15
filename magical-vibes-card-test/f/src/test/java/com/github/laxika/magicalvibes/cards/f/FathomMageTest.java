package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Crocanura;
import com.github.laxika.magicalvibes.cards.i.IncreasingSavagery;
import com.github.laxika.magicalvibes.cards.m.MasterBiomancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FathomMage.class, GrizzlyBears.class, Forest.class})
class FathomMageTest extends BaseCardTest {

    private Permanent addFathomMage(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new FathomMage());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(activePlayer, List.of(new Forest()));
    }

    private void castBearsAndResolveTriggers() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Evolve counter triggers an optional draw that can be accepted")
    void evolveCounterAllowsDraw() {
        Permanent mage = addFathomMage(player1);
        setUpMainPhase(player1);

        castBearsAndResolveTriggers();

        assertThat(mage.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the optional draw leaves the hand untouched")
    void decliningDrawKeepsHand() {
        addFathomMage(player1);
        setUpMainPhase(player1);

        castBearsAndResolveTriggers();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A creature that does not exceed its stats gives no counter and no draw")
    void noEvolveNoDraw() {
        Permanent mage = addFathomMage(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new FathomMage()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mage.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({MasterBiomancer.class})
    void enteringWithTwoCountersOffersTwoIndependentDraws() {
        harness.addToBattlefield(player1, new MasterBiomancer());
        setUpMainPhase(player1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.castFromHand(player1, new FathomMage(), "{2}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).getPlusOnePlusOneCounters()).isEqualTo(2);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({IncreasingSavagery.class})
    void fiveCountersPlacedTogetherOfferFiveDraws() {
        Permanent mage = addFathomMage(player1);
        setUpMainPhase(player1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new IncreasingSavagery()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, mage.getId());

        assertThat(mage.getPlusOnePlusOneCounters()).isEqualTo(5);
        for (int i = 0; i < 5; i++) {
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
            harness.handleMayAbilityChosen(player1, true);
        }
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Crocanura.class})
    void greaterToughnessAloneCausesEvolveAndDraw() {
        Permanent mage = addFathomMage(player1);
        setUpMainPhase(player1);
        harness.castFromHand(player1, new Crocanura(), "{2}{G}");
        resolveAllTriggers();

        assertThat(mage.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentsCreatureDoesNotCauseEvolve() {
        Permanent mage = addFathomMage(player1);
        setUpMainPhase(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(mage.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void evolveRechecksStatsAtResolution() {
        Permanent mage = addFathomMage(player1);
        setUpMainPhase(player1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        mage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(mage.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
