package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FoulImp;
import com.github.laxika.magicalvibes.cards.f.FurnaceSpirit;
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

@CardUsed({Mindwarper.class, FoulImp.class, FurnaceSpirit.class})
class MindwarperTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreePlusOneCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Mindwarper()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mindwarper = findPermanent(player1, "Mindwarper");
        assertThat(mindwarper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(mindwarper.getEffectivePower()).isEqualTo(3);
        assertThat(mindwarper.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes a counter and makes the targeted player discard")
    void removesCounterAndMakesTargetPlayerDiscard() {
        harness.setHand(player1, List.of(new FoulImp()));
        Permanent mindwarper = addReadyMindwarper(player1, 3);
        prepareSorcerySpeedActivation();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, player1.getId());
        assertThat(mindwarper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Foul Imp");
    }

    @Test
    @DisplayName("Can target the opponent to discard exactly one card")
    void targetsOpponentToDiscardOneCard() {
        harness.setHand(player2, List.of(new FoulImp(), new FoulImp()));
        Permanent mindwarper = addReadyMindwarper(player1, 3);
        prepareSorcerySpeedActivation();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(mindwarper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Foul Imp");
    }

    @Test
    @DisplayName("The ability still resolves after removing the last counter")
    void resolvesAfterRemovingLastCounter() {
        harness.setHand(player2, List.of(new FoulImp()));
        Permanent mindwarper = addReadyMindwarper(player1, 1);
        prepareSorcerySpeedActivation();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mindwarper);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Foul Imp");
    }

    @Test
    @DisplayName("Cannot activate without a +1/+1 counter")
    void cannotActivateWithoutCounters() {
        Permanent mindwarper = addReadyMindwarper(player1, 0);
        harness.addToBattlefield(player2, new FurnaceSpirit());
        prepareSorcerySpeedActivation();
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
        assertThat(mindwarper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot activate outside sorcery speed")
    void cannotActivateOutsideSorcerySpeed() {
        addReadyMindwarper(player1, 1);
        prepareSorcerySpeedActivation();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        addReadyMindwarper(player1, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FurnaceSpirit());
        prepareSorcerySpeedActivation();
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMindwarper(Player player, int counters) {
        Permanent perm = addCreatureReady(player, new Mindwarper());
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return perm;
    }

    private void prepareSorcerySpeedActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
