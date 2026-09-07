package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(NamelessOne.class)
class NamelessOneTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualNumberOfWizardsOnAllBattlefields() {
        Permanent namelessOne = addCreatureReady(player1, new NamelessOne());

        assertThat(gqs.getEffectivePower(gd, namelessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, namelessOne)).isEqualTo(1);

        addCreatureReady(player2, new NamelessOne());
        addCreatureReady(player1, new NamelessOne());

        assertThat(gqs.getEffectivePower(gd, namelessOne)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, namelessOne)).isEqualTo(3);
    }

    @Test
    void updatesWhenWizardsLeaveTheBattlefield() {
        Permanent namelessOne = addCreatureReady(player1, new NamelessOne());
        Permanent otherWizard = addCreatureReady(player2, new NamelessOne());

        assertThat(gqs.getEffectivePower(gd, namelessOne)).isEqualTo(2);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, otherWizard));

        assertThat(gqs.getEffectivePower(gd, namelessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, namelessOne)).isEqualTo(1);
    }
}
