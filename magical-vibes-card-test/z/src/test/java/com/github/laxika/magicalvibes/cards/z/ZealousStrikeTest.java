package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.s.ScrollOfAvacyn;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZealousStrike.class, MoorlandInquisitor.class, ScrollOfAvacyn.class})
class ZealousStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +2/+2 and first strike to target creature")
    void grantsBoostAndFirstStrike() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new ZealousStrike()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Boost and first strike wear off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new ZealousStrike()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Can target an opponent's creature without affecting other creatures")
    void canTargetOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new ZealousStrike()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not affect a replacement permanent when its target leaves before resolution")
    void targetLeavingBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new ZealousStrike()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Zealous Strike");
        assertThat(replacement.getPowerModifier()).isZero();
        assertThat(replacement.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player1, new ScrollOfAvacyn());
        harness.setHand(player1, List.of(new ZealousStrike()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Scroll of Avacyn");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
