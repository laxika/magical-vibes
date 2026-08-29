package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DreadStatuaryTest extends BaseCardTest {

    @Test
    void tappingProducesColorlessMana() {
        Permanent statuary = addStatuaryReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(statuary);

        gs.tapPermanent(gd, player1, index);

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
        Permanent permanent = new Permanent(new DreadStatuary());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
