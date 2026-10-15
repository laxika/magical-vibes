package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlyingOctobot;
import com.github.laxika.magicalvibes.cards.p.ProfessionalWrestler;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectacularTactics.class, ProfessionalWrestler.class, FlyingOctobot.class})
class SpectacularTacticsTest extends BaseCardTest {

    @Test
    void counterModeBoostsOwnCreatureAndGrantsHexproof() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FlyingOctobot());

        castMode(0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void counterModeCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlyingOctobot());

        assertThatThrownBy(() -> castMode(0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void destructionModeDestroysCreatureWithPowerAtLeastFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ProfessionalWrestler());

        castMode(1, target.getId());

        harness.assertNotOnBattlefield(player2, "Professional Wrestler");
        harness.assertInGraveyard(player2, "Professional Wrestler");
    }

    @Test
    void destructionModeRejectsCreatureWithPowerThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlyingOctobot());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> castMode(1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    void hexproofExpiresButCounterRemainsAfterTurnEnds() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FlyingOctobot());

        castMode(0, target.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    void counterModeDoesNotDestroyOwnCreatureWithPowerFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ProfessionalWrestler());

        castMode(0, target.getId());

        harness.assertOnBattlefield(player1, "Professional Wrestler");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void destructionModeCanDestroyOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ProfessionalWrestler());

        castMode(1, target.getId());

        harness.assertNotOnBattlefield(player1, "Professional Wrestler");
        harness.assertInGraveyard(player1, "Professional Wrestler");
    }

    @Test
    void destructionModeUsesPowerIncludingCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlyingOctobot());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castMode(1, target.getId());

        harness.assertNotOnBattlefield(player2, "Flying Octobot");
        harness.assertInGraveyard(player2, "Flying Octobot");
    }

    @Test
    void destructionModeDoesNothingIfPowerFallsBelowFourBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlyingOctobot());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        castModeWithoutResolving(1, target.getId());

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Flying Octobot");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        harness.assertInGraveyard(player1, "Spectacular Tactics");
    }

    @Test
    void counterModeDoesNothingIfControlChangesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FlyingOctobot());
        castModeWithoutResolving(0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        harness.assertInGraveyard(player1, "Spectacular Tactics");
    }

    @Test
    void counterModeProtectsCreatureFromOpponentsPendingDestructionSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ProfessionalWrestler());
        harness.setHand(player2, List.of(new SpectacularTactics()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 1, target.getId());

        castMode(0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Professional Wrestler");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        harness.assertInGraveyard(player1, "Spectacular Tactics");
        harness.assertInGraveyard(player2, "Spectacular Tactics");
        assertThat(gd.stack).isEmpty();
    }

    private void castMode(int modeIndex, UUID targetId) {
        castModeWithoutResolving(modeIndex, targetId);
        harness.passBothPriorities();
    }

    private void castModeWithoutResolving(int modeIndex, UUID targetId) {
        harness.setHand(player1, List.of(new SpectacularTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, modeIndex, targetId);
    }
}
