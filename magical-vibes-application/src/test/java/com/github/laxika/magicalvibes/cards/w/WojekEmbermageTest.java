package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WojekEmbermage.class, GrizzlyBears.class, HillGiant.class, Island.class, Ornithopter.class})
class WojekEmbermageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to the target and every creature sharing a color with it")
    void damagesTargetAndColorSharingCreatures() {
        Permanent embermage = addReadyEmbermage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        activate(embermage, target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(matchingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(differentColorCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A colorless target does not damage other colorless creatures")
    void colorlessTargetOnlyDamagesItself() {
        Permanent embermage = addReadyEmbermage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent otherColorlessCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activate(embermage, target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(otherColorlessCreature.getMarkedDamage()).isZero();
        assertThat(coloredCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        Permanent embermage = addReadyEmbermage();
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> activate(embermage, island))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyEmbermage() {
        Permanent embermage = harness.addToBattlefieldAndReturn(player1, new WojekEmbermage());
        embermage.setSummoningSick(false);
        return embermage;
    }

    private void activate(Permanent embermage, Permanent target) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(embermage),
                null, target.getId());
        harness.passBothPriorities();
    }
}
