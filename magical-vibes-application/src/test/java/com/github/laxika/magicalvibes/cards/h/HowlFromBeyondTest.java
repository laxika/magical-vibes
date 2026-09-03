package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MinionOfLeshrac;
import com.github.laxika.magicalvibes.cards.w.WindSpirit;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
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

@CardUsed({HowlFromBeyond.class, WindSpirit.class, ZuranOrb.class, MinionOfLeshrac.class, GrizzlyBears.class, Forest.class})
class HowlFromBeyondTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives target creature +X/+0")
    void resolvesAndBoostsPowerOnly() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new WindSpirit());
        harness.setHand(player1, List.of(new HowlFromBeyond()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 4, spirit.getId());
        harness.passBothPriorities();

        assertThat(spirit.getPowerModifier()).isEqualTo(4);
        assertThat(spirit.getToughnessModifier()).isEqualTo(0);
        assertThat(spirit.getEffectivePower()).isEqualTo(7);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new WindSpirit());
        harness.setHand(player1, List.of(new HowlFromBeyond()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, 3, spirit.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spirit.getPowerModifier()).isEqualTo(0);
        assertThat(spirit.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new WindSpirit()); // legal creature target so the spell is castable (CR 601.2c)
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());
        harness.setHand(player1, List.of(new HowlFromBeyond()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, orb.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new WindSpirit());
        harness.setHand(player1, List.of(new HowlFromBeyond()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, 1, spirit.getId());
        harness.passBothPriorities();

        assertThat(spirit.getPowerModifier()).isEqualTo(1);
        assertThat(spirit.getToughnessModifier()).isEqualTo(0);
        assertThat(spirit.getEffectivePower()).isEqualTo(4);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("X can be zero")
    void resolvesWithZeroBoost() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new WindSpirit());
        harness.setHand(player1, List.of(new HowlFromBeyond()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(spirit.getPowerModifier()).isEqualTo(0);
        assertThat(spirit.getToughnessModifier()).isEqualTo(0);
        assertThat(spirit.getEffectivePower()).isEqualTo(3);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature with protection from black")
    void cannotTargetCreatureWithProtectionFromBlack() {
        harness.addToBattlefield(player1, new WindSpirit());
        Permanent minion = harness.addToBattlefieldAndReturn(player2, new MinionOfLeshrac());
        harness.setHand(player1, List.of(new HowlFromBeyond()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, minion.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("X can be zero")
    void resolvesWithZeroX() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HowlFromBeyond()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(gameLogContains("gets +0/+0")).isTrue();
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HowlFromBeyond()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 2, bear.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Howl from Beyond");
    }
}
