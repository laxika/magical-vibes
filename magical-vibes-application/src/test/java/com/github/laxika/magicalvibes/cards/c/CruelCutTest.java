package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelCut.class, GrizzlyBears.class, HillGiant.class, Plains.class})
class CruelCutTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with power 2 or less")
    void destroysSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCruelCut(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetLargeCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareCruelCut();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareCruelCut();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castCruelCut(Permanent target) {
        prepareCruelCut();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareCruelCut() {
        harness.setHand(player1, List.of(new CruelCut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
