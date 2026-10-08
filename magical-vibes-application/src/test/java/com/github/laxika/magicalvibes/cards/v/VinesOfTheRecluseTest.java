package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.s.StalkingDrone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VinesOfTheRecluse.class, StalkingDrone.class, BoneSaw.class})
class VinesOfTheRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Vines of the Recluse untaps, boosts, and grants reach to target creature")
    void untapsBoostsAndGrantsReach() {
        Permanent target = addTappedCreature(player2);

        castVinesOfTheRecluse(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Boost and reach expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = addTappedCreature(player2);
        castVinesOfTheRecluse(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addTappedCreature(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BoneSaw());
        harness.setHand(player1, List.of(new VinesOfTheRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An already untapped creature you control still gets the boost and reach")
    void boostsAlreadyUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StalkingDrone());

        castVinesOfTheRecluse(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Replacing the untap with removal of a stun counter does not prevent the boost or reach")
    void grantsEffectsEvenWhenUntapIsReplaced() {
        Permanent target = addTappedCreature(player1);
        target.setCounterCount(CounterType.STUN, 1);

        castVinesOfTheRecluse(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("The spell has no effect when its target leaves before resolution")
    void doesNotAffectOtherCreaturesWhenTargetLeaves() {
        Permanent target = addTappedCreature(player2);
        Permanent other = addTappedCreature(player1);
        harness.setHand(player1, List.of(new VinesOfTheRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vines of the Recluse");
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, other, Keyword.REACH)).isFalse();
    }

    private void castVinesOfTheRecluse(Permanent target) {
        harness.setHand(player1, List.of(new VinesOfTheRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new StalkingDrone());
        perm.tap();
        return perm;
    }
}
