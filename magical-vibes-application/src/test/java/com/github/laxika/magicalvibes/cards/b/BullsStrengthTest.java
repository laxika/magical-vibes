package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArmoryVeteran;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
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

@CardUsed({BullsStrength.class, ArmoryVeteran.class, Mountain.class, PowerWordKill.class})
class BullsStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, boosts, and grants trample to target creature")
    void untapsBoostsAndGrantsTrample() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoryVeteran());
        target.tap();
        castBullsStrength(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArmoryVeteran());
        castBullsStrength(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new ArmoryVeteran());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new BullsStrength()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost and grant trample to an already untapped creature")
    void boostsAlreadyUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArmoryVeteran());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ArmoryVeteran());
        other.tap();

        castBullsStrength(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        assertThat(other.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not resolve when the target is destroyed in response")
    void doesNotResolveWhenTargetIsDestroyed() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArmoryVeteran());
        target.tap();
        harness.setHand(player1, List.of(new BullsStrength()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Armory Veteran");
        harness.assertInGraveyard(player1, "Armory Veteran");
        harness.assertInGraveyard(player1, "Bull's Strength");
        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private void castBullsStrength(Permanent target) {
        harness.setHand(player1, List.of(new BullsStrength()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
