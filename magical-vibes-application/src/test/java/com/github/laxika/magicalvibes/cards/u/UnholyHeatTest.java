package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnholyHeat.class, DarksteelRelic.class, GrizzlyBears.class, HillGiant.class, Pacifism.class,
        Shock.class})
class UnholyHeatTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage without delirium")
    void dealsTwoDamageWithoutDelirium() {
        Permanent target = addHillGiant();

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Deals 6 damage with delirium")
    void dealsSixDamageWithDelirium() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new DarksteelRelic(),
                new Pacifism()));
        Permanent target = addHillGiant();

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addHillGiant() {
        Permanent target = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player2.getId()).add(target);
        return target;
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
