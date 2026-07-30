package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarkFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dark Favor attaches it, boosts the creature by +3/+1 and costs 1 life")
    void castAttachesBoostsAndCostsLife() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new DarkFavor()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dark Favor")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost goes away when Dark Favor leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new DarkFavor());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
