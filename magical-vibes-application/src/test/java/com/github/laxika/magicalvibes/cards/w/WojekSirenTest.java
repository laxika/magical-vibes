package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WojekSiren.class, GrizzlyBears.class, HillGiant.class, Ornithopter.class})
class WojekSirenTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the target and every creature sharing a color with it")
    void boostsTargetAndColorSharingCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownMatchingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentMatchingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new WojekSiren()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownMatchingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownMatchingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentMatchingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentMatchingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, differentColorCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, differentColorCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A colorless target does not boost other colorless creatures")
    void colorlessTargetOnlyBoostsItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent otherColorlessCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WojekSiren()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherColorlessCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, otherColorlessCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, coloredCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, coloredCreature)).isEqualTo(2);
    }
}
