package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.StalkingDrone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ChandraFlamecaller.class, StalkingDrone.class})
class ChandraFlamecallerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates two hasty Elementals and exiles them at the next end step")
    void plusOneCreatesHastyElementalsUntilNextEndStep() {
        Permanent chandra = addReadyChandra(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        List<Permanent> elementals = findPermanents(player1, "Elemental");
        assertThat(elementals).hasSize(2);
        assertThat(elementals).allSatisfy(elemental -> {
            assertThat(elemental.getEffectivePower()).isEqualTo(3);
            assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
            assertThat(elemental.getCard().getKeywords()).contains(Keyword.HASTE);
        });

        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("0 discards the hand and draws one more card than discarded")
    void zeroDiscardsHandAndDrawsOneMore() {
        Permanent chandra = addReadyChandra(player1, 4);
        harness.setHand(player1, List.of(new StalkingDrone(), new StalkingDrone()));
        harness.setLibrary(player1, List.of(new StalkingDrone(), new StalkingDrone(), new StalkingDrone()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("-X deals X damage to every creature")
    void minusXDamagesEveryCreature() {
        Permanent chandra = addReadyChandra(player1, 4);
        harness.addToBattlefield(player1, new StalkingDrone());
        harness.addToBattlefield(player2, new StalkingDrone());

        harness.activateAbility(player1, 0, 2, 2, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Stalking Drone");
        harness.assertNotOnBattlefield(player2, "Stalking Drone");
    }

    @Test
    @DisplayName("Token exile waits for the delayed end-step trigger to resolve")
    void tokensRemainUntilDelayedExileTriggerResolves() {
        addReadyChandra(player1, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);

        assertThat(findPermanents(player1, "Elemental")).hasSize(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("0 draws one card from an empty hand")
    void zeroDrawsOneWithEmptyHand() {
        Permanent chandra = addReadyChandra(player1, 4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new StalkingDrone(), new StalkingDrone()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-X can use zero without damaging creatures or players")
    void minusZeroDealsNoDamage() {
        Permanent chandra = addReadyChandra(player1, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StalkingDrone());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, 0, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Stalking Drone");
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("-X still resolves after spending all of Chandra's loyalty")
    void minusXResolvesAfterChandraDies() {
        addReadyChandra(player1, 4);
        harness.addToBattlefield(player1, new StalkingDrone());
        harness.addToBattlefield(player2, new StalkingDrone());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, 4, null);

        harness.assertNotOnBattlefield(player1, "Chandra, Flamecaller");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof ChandraFlamecaller);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stalking Drone");
        harness.assertNotOnBattlefield(player2, "Stalking Drone");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("-X cannot spend more loyalty than Chandra has")
    void minusXCannotExceedLoyalty() {
        Permanent chandra = addReadyChandra(player1, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 5, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ChandraFlamecaller());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
