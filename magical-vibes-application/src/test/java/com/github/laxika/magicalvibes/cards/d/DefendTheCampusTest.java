package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefendTheCampus.class, AirElemental.class, GrizzlyBears.class, HillGiant.class})
class DefendTheCampusTest extends BaseCardTest {

    @Test
    void boostsOnlyCreaturesYouControl() {
        Permanent mine = addCreatureReady(player1, new GrizzlyBears());
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DefendTheCampus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(mine.getPowerModifier()).isEqualTo(1);
        assertThat(mine.getToughnessModifier()).isEqualTo(1);
        assertThat(theirs.getPowerModifier()).isZero();
        assertThat(theirs.getToughnessModifier()).isZero();
    }

    @Test
    void destroysCreatureWithPowerAtLeastFour() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new DefendTheCampus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 1, List.of(elemental.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void cannotTargetCreatureWithPowerLessThanFour() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DefendTheCampus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(giant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
