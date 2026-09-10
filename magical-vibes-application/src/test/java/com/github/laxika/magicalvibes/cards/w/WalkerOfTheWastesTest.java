package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WalkerOfTheWastes.class, Wastes.class, Forest.class})
class WalkerOfTheWastesTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each Wastes you control")
    void getsBonusForControlledWastes() {
        Permanent walker = addCreatureReady(player1, new WalkerOfTheWastes());
        harness.addToBattlefield(player1, new Wastes());
        harness.addToBattlefield(player1, new Wastes());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Wastes());

        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, walker)).isEqualTo(6);
    }

    @Test
    @DisplayName("The bonus updates as your Wastes enter and leave")
    void bonusUpdatesWhenWastesChange() {
        Permanent walker = addCreatureReady(player1, new WalkerOfTheWastes());

        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, walker)).isEqualTo(4);

        harness.addToBattlefield(player1, new Wastes());
        harness.addToBattlefield(player1, new Wastes());
        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, walker)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Wastes"));
        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, walker)).isEqualTo(4);
    }
}
