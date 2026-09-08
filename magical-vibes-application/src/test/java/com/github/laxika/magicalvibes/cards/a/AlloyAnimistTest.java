package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlloyAnimistTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a noncreature artifact you control into a 4/4 artifact creature")
    void animatesControlledArtifact() {
        addCreatureReady(player1, new AlloyAnimist());
        harness.addToBattlefield(player1, new Millstone());
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent millstone = findPermanent(player1, "Millstone");
        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(gqs.isArtifact(millstone)).isTrue();
        assertThat(millstone.getEffectivePower()).isEqualTo(4);
        assertThat(millstone.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("The animation wears off at end of turn")
    void animationEndsAtEndOfTurn() {
        addCreatureReady(player1, new AlloyAnimist());
        harness.addToBattlefield(player1, new Millstone());
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent millstone = findPermanent(player1, "Millstone");
        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, millstone)).isFalse();
        assertThat(gqs.isArtifact(millstone)).isTrue();
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        addCreatureReady(player1, new AlloyAnimist());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent ironMyr = findPermanent(player1, "Iron Myr");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ironMyr.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact you control");
    }
}
