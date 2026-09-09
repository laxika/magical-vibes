package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaronaFalseGod.class, GrizzlyBears.class, ElvishWarrior.class})
class KaronaFalseGodTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's upkeep untaps Karona and gives that player control")
    void eachPlayersUpkeepUntapsAndGivesControl() {
        Permanent karona = harness.addToBattlefieldAndReturn(player1, new KaronaFalseGod());
        karona.tap();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(karona.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(karona);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(karona);
    }

    @Test
    @DisplayName("Attacking lets you choose a creature type to boost across all battlefields")
    void attackingBoostsChosenCreatureType() {
        Permanent karona = addCreatureReady(player1, new KaronaFalseGod());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent elf = addCreatureReady(player2, new ElvishWarrior());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, karona)).isEqualTo(5);
    }

}
