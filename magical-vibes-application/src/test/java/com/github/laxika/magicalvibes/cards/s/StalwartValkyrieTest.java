package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StalwartValkyrie.class, GiantGrowth.class, GrizzlyBears.class})
class StalwartValkyrieTest extends BaseCardTest {

    @Test
    void alternateCostExilesChosenCreatureCardFromGraveyard() {
        StalwartValkyrie valkyrie = new StalwartValkyrie();
        GiantGrowth noncreature = new GiantGrowth();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player1, List.of(valkyrie));
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .extracting(Permanent::getCard)
                .isSameAs(valkyrie);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void alternateCostRequiresACreatureCardAndDoesNotPayManaOnFailure() {
        GiantGrowth noncreature = new GiantGrowth();
        harness.setHand(player1, List.of(new StalwartValkyrie()));
        harness.setGraveyard(player1, List.of(noncreature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }
}
