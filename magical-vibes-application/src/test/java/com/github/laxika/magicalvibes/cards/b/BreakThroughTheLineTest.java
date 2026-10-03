package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
import com.github.laxika.magicalvibes.cards.s.SandsteppeOutcast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreakThroughTheLine.class, SandsteppeOutcast.class, FeralKrushok.class})
class BreakThroughTheLineTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants haste and unblockability to the target")
    void resolvingGrantsHasteAndUnblockability() {
        addReadyBreakThroughTheLine(player1);
        Permanent target = addCreatureReady(player2, new SandsteppeOutcast());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Haste and unblockability wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        addReadyBreakThroughTheLine(player1);
        Permanent target = addCreatureReady(player2, new SandsteppeOutcast());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        addReadyBreakThroughTheLine(player1);
        Permanent giant = addCreatureReady(player2, new FeralKrushok());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityDoesNotResolveIfTargetPowerIncreasesAboveTwo() {
        addReadyBreakThroughTheLine(player1);
        Permanent target = addCreatureReady(player2, new SandsteppeOutcast());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void effectsRemainIfTargetPowerIncreasesAfterResolution() {
        addReadyBreakThroughTheLine(player1);
        Permanent target = addCreatureReady(player2, new SandsteppeOutcast());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void canActivateRepeatedlyForOwnSummoningSickCreatures() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BreakThroughTheLine());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SandsteppeOutcast());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SandsteppeOutcast());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
        assertThat(second.isCantBeBlocked()).isTrue();
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent source = addReadyBreakThroughTheLine(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBreakThroughTheLine(Player player) {
        return addCreatureReady(player, new BreakThroughTheLine());
    }
}
