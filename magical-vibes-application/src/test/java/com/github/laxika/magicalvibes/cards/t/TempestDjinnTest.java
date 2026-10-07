package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HallowedFountain;
import com.github.laxika.magicalvibes.cards.i.InBolassClutches;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempestDjinn.class, Island.class, Forest.class, InBolassClutches.class, HallowedFountain.class})
class TempestDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Base stats are 0/4 with no basic Islands")
    void baseStatsWithNoIslands() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player1, new TempestDjinn());

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+0 for each basic Island you control")
    void boostsWithBasicIslands() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player1, new TempestDjinn());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's basic Islands do not contribute to the bonus")
    void opponentIslandsDontCount() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player1, new TempestDjinn());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Non-Island basic lands do not contribute to the bonus")
    void nonIslandLandsDontCount() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player1, new TempestDjinn());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        // Only 1 basic Island, Forest does not count
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus updates when a basic Island leaves the battlefield")
    void bonusUpdatesWhenIslandLeaves() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player1, new TempestDjinn());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Island"));

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }


    @Test
    @DisplayName("Bonus updates immediately when a basic Island enters the battlefield")
    void bonusUpdatesWhenIslandEnters() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player1, new TempestDjinn());
        assertThat(gqs.getEffectivePower(gd, djinn)).isZero();

        harness.enterBattlefieldAndReturn(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("An Island without the basic supertype does not contribute to the bonus")
    void nonbasicIslandDoesNotCount() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player1, new TempestDjinn());
        harness.addToBattlefield(player1, new HallowedFountain());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("After changing control, the bonus counts the new controller's basic Islands")
    void bonusUsesNewControllersIslands() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player2, new TempestDjinn());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player1, new Island());
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(2);

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castEnchantment(player1, 0, djinn.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(djinn);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(djinn);
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }
}
