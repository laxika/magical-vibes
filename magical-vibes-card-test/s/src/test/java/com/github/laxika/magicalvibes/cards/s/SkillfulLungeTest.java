package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkillfulLunge.class, DawntreaderElk.class})
class SkillfulLungeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Skillful Lunge puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new SkillfulLunge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Skillful Lunge gives +2/+0 and first strike to target creature")
    void resolvingBoostsAndGrantsFirstStrike() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new SkillfulLunge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new SkillfulLunge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Skillful Lunge fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new SkillfulLunge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Skillful Lunge");
    }

    @Test
    @DisplayName("Skillful Lunge can boost an opponent's creature without affecting other creatures")
    void canTargetOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new SkillfulLunge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Two Skillful Lunges add their power boosts and both expire at cleanup")
    void repeatedCastsStackAndExpire() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new SkillfulLunge(), new SkillfulLunge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Skillful Lunge cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SkillfulLunge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Skillful Lunge");
    }

    @Test
    @DisplayName("Granted first strike kills a blocker before it can deal damage")
    void grantedFirstStrikeWinsCombat() {
        Permanent attacker = addCreatureReady(player1, new DawntreaderElk());
        Permanent blocker = addCreatureReady(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new SkillfulLunge()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        harness.assertOnBattlefield(player1, "Dawntreader Elk");
        harness.assertInGraveyard(player2, "Dawntreader Elk");
        harness.assertNotOnBattlefield(player2, "Dawntreader Elk");
        harness.assertLife(player2, 20);
    }
}
