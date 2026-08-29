package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StandingStones.class})
class StandingStonesTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, {T}, Pay 1 life adds one mana of the chosen color")
    void addsChosenColorAfterPayingCost() {
        Permanent standingStones = harness.addToBattlefieldAndReturn(player1, new StandingStones());
        standingStones.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(standingStones.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
