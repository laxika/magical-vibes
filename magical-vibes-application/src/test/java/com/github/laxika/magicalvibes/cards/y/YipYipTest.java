package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.w.WhiteLotusTile;
import com.github.laxika.magicalvibes.cards.p.PlatypusBear;
import com.github.laxika.magicalvibes.cards.w.WaterTribeRallier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YipYip.class, WaterTribeRallier.class, PlatypusBear.class, WhiteLotusTile.class})
class YipYipTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature you control +2/+2 and an Ally flying")
    void boostsAllyAndGrantsFlying() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new WaterTribeRallier());
        harness.setHand(player1, java.util.List.of(new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.getPowerModifier()).isEqualTo(2);
        assertThat(ally.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Gives a non-Ally creature only +2/+2")
    void boostsNonAllyWithoutFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PlatypusBear());
        harness.setHand(player1, java.util.List.of(new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The boost and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new WaterTribeRallier());
        harness.setHand(player1, java.util.List.of(new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, ally.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(ally.getPowerModifier()).isZero();
        assertThat(ally.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PlatypusBear());
        harness.setHand(player1, java.util.List.of(new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new WhiteLotusTile());
        harness.setHand(player1, java.util.List.of(new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Only the targeted Ally receives the boost and flying")
    void doesNotAffectOtherAllies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WaterTribeRallier());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new WaterTribeRallier());
        harness.setHand(player1, java.util.List.of(new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Multiple copies stack their boosts and all expire at cleanup")
    void multipleCopiesStackUntilEndOfTurn() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new WaterTribeRallier());
        harness.setHand(player1, java.util.List.of(new YipYip(), new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, ally.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.getPowerModifier()).isEqualTo(4);
        assertThat(ally.getToughnessModifier()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.FLYING)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(ally.getPowerModifier()).isZero();
        assertThat(ally.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not affect another Ally when its target has left the battlefield")
    void targetRemovedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WaterTribeRallier());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new WaterTribeRallier());
        harness.setHand(player1, java.util.List.of(new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setGraveyard(player1, java.util.List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        harness.assertInGraveyard(player1, "Yip Yip!");
        assertThat(gd.stack).isEmpty();
    }
}
