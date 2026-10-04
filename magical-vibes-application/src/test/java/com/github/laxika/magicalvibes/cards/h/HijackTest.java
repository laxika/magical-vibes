package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CobbledWings;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.s.SunbirdsInvocation;
import com.github.laxika.magicalvibes.cards.w.WakeThrasher;
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

@CardUsed({Hijack.class, JungleDelver.class, CobbledWings.class, SunbirdsInvocation.class})
class HijackTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Hijack on a creature untaps it, gains control, and grants haste")
    void resolvesOnCreature() {
        Permanent target = addCreatureReady(player2, new JungleDelver());
        target.tap();
        harness.setHand(player1, List.of(new Hijack()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Resolving Hijack on an artifact untaps it, gains control, and grants haste")
    void resolvesOnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CobbledWings());
        target.tap();
        harness.setHand(player1, List.of(new Hijack()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Stolen creature can attack this turn because Hijack grants haste")
    void stolenCreatureCanAttackDueToHaste() {
        Permanent target = addCreatureReady(player2, new JungleDelver());
        target.setSummoningSick(false);
        harness.setHand(player1, List.of(new Hijack()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);

        declareAttackers(player1, List.of(attackerIndex));

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hijack control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new JungleDelver());
        harness.setHand(player1, List.of(new Hijack()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addCreatureReady(player1, new JungleDelver()); // valid target so spell is playable
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new SunbirdsInvocation());
        harness.setHand(player1, List.of(new Hijack()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Hijack fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player2, new JungleDelver());
        harness.setHand(player1, List.of(new Hijack()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    void canUntapAndGrantHasteToOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        target.tap();
        harness.setHand(player1, List.of(new Hijack()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void noncreatureArtifactGainsHasteUntilCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CobbledWings());
        harness.setHand(player1, List.of(new Hijack()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @CardUsed({WakeThrasher.class})
    void gainsControlBeforeUntappingForControllerSensitiveTriggers() {
        Permanent ownThrasher = harness.addToBattlefieldAndReturn(player1, new WakeThrasher());
        Permanent opposingThrasher = harness.addToBattlefieldAndReturn(player2, new WakeThrasher());
        Permanent target = addCreatureReady(player2, new JungleDelver());
        target.tap();
        harness.setHand(player1, List.of(new Hijack()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(ownThrasher.getPowerModifier()).isEqualTo(1);
        assertThat(ownThrasher.getToughnessModifier()).isEqualTo(1);
        assertThat(opposingThrasher.getPowerModifier()).isZero();
        assertThat(opposingThrasher.getToughnessModifier()).isZero();
    }
}
