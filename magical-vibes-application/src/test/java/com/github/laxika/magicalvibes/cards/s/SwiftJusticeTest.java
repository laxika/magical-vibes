package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwiftJustice.class, DrudgeBeetle.class})
class SwiftJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Swift Justice gives the target +1/+0, first strike and lifelink")
    void resolvingBoostsAndGrantsKeywords() {
        harness.addToBattlefield(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new SwiftJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Drudge Beetle");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Swift Justice can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new SwiftJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Drudge Beetle");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Boost and keywords wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new SwiftJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Drudge Beetle");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Swift Justice fizzles if the target leaves the battlefield")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new SwiftJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Drudge Beetle");
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Swift Justice");
    }

    @Test
    @DisplayName("First strike kills the blocker before it deals damage and lifelink gains full damage")
    void firstStrikeAndLifelinkWorkInCombat() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        attacker.setSummoningSick(false);
        blocker.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SwiftJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.runStateBasedActions();
        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player1, "Drudge Beetle");
        harness.assertInGraveyard(player2, "Drudge Beetle");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lifelink benefits the opposing creature's controller, not Swift Justice's caster")
    void opposingCreatureControllerGainsLife() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        attacker.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SwiftJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.resolveCombatDamage();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 23);
    }
}
