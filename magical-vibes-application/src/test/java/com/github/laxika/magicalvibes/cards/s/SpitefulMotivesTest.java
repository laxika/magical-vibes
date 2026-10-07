package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.ThornhideWolves;
import com.github.laxika.magicalvibes.cards.v.VesselOfVolatility;
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

@CardUsed({SpitefulMotives.class, ThornhideWolves.class, VesselOfVolatility.class})
class SpitefulMotivesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Spiteful Motives attaches it and grants +3/+0 and first strike")
    void resolvingAttachesBoostsAndGrantsFirstStrike() {
        Permanent wolves = harness.addToBattlefieldAndReturn(player1, new ThornhideWolves());
        harness.setHand(player1, List.of(new SpitefulMotives()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, wolves.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached() && wolves.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, wolves)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, wolves)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, wolves, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Removing Spiteful Motives removes its boost and first strike")
    void effectsStopWhenRemoved() {
        Permanent wolves = harness.addToBattlefieldAndReturn(player1, new ThornhideWolves());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpitefulMotives());
        aura.setAttachedTo(wolves.getId());

        assertThat(gqs.getEffectivePower(gd, wolves)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, wolves, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, wolves)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolves)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, wolves, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Spiteful Motives cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new VesselOfVolatility());
        harness.setHand(player1, List.of(new SpitefulMotives()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Flash allows enchanting an opposing creature during its controller's end step")
    void canEnchantOpposingCreatureDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThornhideWolves());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ThornhideWolves());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);
        harness.setHand(player1, List.of(new SpitefulMotives()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached() && target.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
    }
}
