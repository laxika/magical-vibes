package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreadStatuary.class})
class DreadStatuaryTest extends BaseCardTest {

    @Test
    void tappingProducesColorlessMana() {
        addStatuaryReady(player1);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void animatesIntoA4x2ArtifactGolemAndRemainsALand() {
        Permanent statuary = addStatuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, statuary)).isTrue();
        assertThat(gqs.isLand(gd, statuary)).isTrue();
        assertThat(gqs.isArtifact(statuary)).isTrue();
        assertThat(gqs.getEffectivePower(gd, statuary)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, statuary)).isEqualTo(2);
        assertThat(statuary.getTransientSubtypes()).containsExactly(CardSubtype.GOLEM);
    }

    @Test
    void animationEndsAtEndOfTurn() {
        Permanent statuary = addStatuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, statuary)).isFalse();
        assertThat(gqs.isLand(gd, statuary)).isTrue();
        assertThat(gqs.isArtifact(statuary)).isFalse();
        assertThat(statuary.getTransientSubtypes()).isEmpty();
    }

    @Test
    void activatingAnimationDoesNotTapTheLand() {
        Permanent statuary = addStatuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(statuary.isTapped()).isFalse();
    }

    private Permanent addStatuaryReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new DreadStatuary());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    void tappedLandCanAnimateAndStaysTapped() {
        Permanent statuary = addStatuaryReady(player1);
        harness.tapPermanent(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gqs.isCreature(gd, statuary)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, statuary)).isTrue();
        assertThat(statuary.isTapped()).isTrue();
    }

    @Test
    void animatedLandRetainsItsManaAbility() {
        Permanent statuary = addStatuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.tapPermanent(player1, 0);

        assertThat(statuary.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, statuary)).isTrue();
    }

    @Test
    void newlyControlledLandCanAnimateButCannotTapForManaAsACreature() {
        Permanent statuary = harness.addToBattlefieldAndReturn(player1, new DreadStatuary());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, statuary)).isTrue();
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(statuary.isTapped()).isFalse();
    }

    @Test
    void newlyControlledUnanimatedLandCanTapForMana() {
        harness.addToBattlefield(player1, new DreadStatuary());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void repeatedAnimationDoesNotIncreasePowerOrToughness() {
        Permanent statuary = addStatuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, statuary)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, statuary)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
