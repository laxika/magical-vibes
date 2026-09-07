package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GruulWarPlow.class, GrizzlyBears.class})
class GruulWarPlowTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have trample")
    void grantsTrampleToOwnCreatures() {
        Permanent plow = harness.addToBattlefieldAndReturn(player1, new GruulWarPlow());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, plow, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The activated ability animates Gruul War Plow into a 4/4 Juggernaut artifact creature")
    void animatesIntoCreature() {
        Permanent plow = harness.addToBattlefieldAndReturn(player1, new GruulWarPlow());
        addAnimationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, plow)).isTrue();
        assertThat(gqs.isArtifact(plow)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plow)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, plow)).isEqualTo(4);
        assertThat(plow.getTransientSubtypes()).contains(CardSubtype.JUGGERNAUT);
        assertThat(gqs.hasKeyword(gd, plow, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The animation ends at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent plow = harness.addToBattlefieldAndReturn(player1, new GruulWarPlow());
        addAnimationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, plow)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, plow)).isFalse();
        assertThat(gqs.isArtifact(plow)).isTrue();
        assertThat(gqs.hasKeyword(gd, plow, Keyword.TRAMPLE)).isFalse();
    }

    private void addAnimationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
