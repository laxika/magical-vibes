package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdaptiveSporesinger.class, CopperLonglegs.class, Plains.class})
class AdaptiveSporesingerTest extends BaseCardTest {

    private static final String BOOST = "Target creature gets +2/+2 and gains vigilance until end of turn";
    private static final String PROLIFERATE = "Proliferate";

    @Test
    @DisplayName("Target creature gets +2/+2 and vigilance until end of turn")
    void boostsAndGrantsVigilance() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());

        enterSporesinger();
        chooseBoost(target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The boost mode only targets creatures")
    void boostModeRejectsNonCreatureTarget() {
        UUID plainsId = harness.addToBattlefieldAndReturn(player2, new Plains()).getId();

        enterSporesinger();
        harness.handleListChoice(player1, BOOST);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, plainsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The proliferate mode adds a counter to a chosen permanent")
    void proliferates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveProliferateMode();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canChooseTheEnteringCreatureAsTheBoostTarget() {
        enterSporesinger();
        UUID sporesingerId = harness.getPermanentId(player1, "Adaptive Sporesinger");
        chooseBoost(sporesingerId);
        harness.passBothPriorities();

        Permanent sporesinger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(sporesingerId))
                .findFirst().orElseThrow();
        assertThat(sporesinger.getEffectivePower()).isEqualTo(4);
        assertThat(sporesinger.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void proliferateCanChooseNoPermanentsOrPlayers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        resolveProliferateMode();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void proliferateAddsEveryCounterKindToChosenPermanentsAndPlayers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        target.setCounterCount(CounterType.OIL, 2);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        resolveProliferateMode();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId(), player2.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void proliferateCanChooseAPlayerWithOnlyEnergyCounters() {
        gd.setPlayerEnergyCounters(player2.getId(), 2);

        resolveProliferateMode();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).contains(player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    void proliferateWithNoCountersFinishesWithoutASelection() {
        resolveProliferateMode();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Adaptive Sporesinger");
    }

    private void enterSporesinger() {
        harness.castFromHand(player1, new AdaptiveSporesinger(), "{2}{G}");
        harness.passBothPriorities();
    }

    private void chooseBoost(UUID targetId) {
        harness.handleListChoice(player1, BOOST);
        harness.handlePermanentChosen(player1, targetId);
    }

    private void resolveProliferateMode() {
        enterSporesinger();
        harness.handleListChoice(player1, PROLIFERATE);
        harness.passBothPriorities();
    }
}
