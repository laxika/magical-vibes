package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JumboCactuar;
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

@CardUsed({MagicDamper.class, Island.class, JumboCactuar.class})
class MagicDamperTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and boosts a creature you control and grants it hexproof")
    void untapsBoostsAndGrantsHexproof() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JumboCactuar());
        target.tap();

        castMagicDamper(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("The boost and hexproof wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JumboCactuar());
        castMagicDamper(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by another player")
    void cannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JumboCactuar());
        harness.setHand(player1, List.of(new MagicDamper()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new MagicDamper()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("An untapped creature can be targeted, and other creatures are unaffected")
    void untappedTargetDoesNotAffectOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JumboCactuar());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new JumboCactuar());
        other.tap();

        castMagicDamper(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.getGrantedKeywords()).contains(Keyword.HEXPROOF);
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("A creature with hexproof can still be targeted by its controller")
    void controllerCanTargetAgainAfterGainingHexproof() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JumboCactuar());
        castMagicDamper(target);
        target.tap();

        castMagicDamper(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("The spell does nothing if the target changes controller before resolution")
    void targetChangingControllerMakesSpellFailToResolve() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JumboCactuar());
        target.tap();
        harness.setHand(player1, List.of(new MagicDamper()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Magic Damper");
    }

    private void castMagicDamper(Permanent target) {
        harness.setHand(player1, List.of(new MagicDamper()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
