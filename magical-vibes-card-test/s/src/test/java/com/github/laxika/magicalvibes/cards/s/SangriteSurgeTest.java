package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SangriteSurge.class, CylianElf.class, ObeliskOfBant.class})
class SangriteSurgeTest extends BaseCardTest {

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Resolving gives target creature +3/+3 and double strike")
    void resolvesBoostAndDoubleStrike() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new SangriteSurge()));
        addCastingMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Boost and double strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new SangriteSurge()));
        addCastingMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new SangriteSurge()));
        addCastingMana();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new CylianElf());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new ObeliskOfBant());
        harness.setHand(player1, List.of(new SangriteSurge()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost an opponent's creature without affecting other creatures")
    void canTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new SangriteSurge()));
        addCastingMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        assertThat(other.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Repeated casts stack the boosts and all temporary effects expire")
    void repeatedCastsStackAndExpire() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new SangriteSurge(), new SangriteSurge()));
        addCastingMana();
        addCastingMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(8);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
