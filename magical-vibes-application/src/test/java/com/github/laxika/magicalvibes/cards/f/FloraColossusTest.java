package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FloraColossus.class, Forest.class, Shock.class})
class FloraColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Flora Colossus power and toughness equal its controller's lands")
    void ptEqualsControlledLands() {
        Permanent flora = addFloraReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, flora)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flora)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flora Colossus updates when its controller's lands change")
    void ptUpdatesWhenLandsChange() {
        Permanent flora = addFloraReady(player1);

        assertThat(gqs.getEffectivePower(gd, flora)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, flora)).isZero();

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, flora)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, flora)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponents cannot target Flora Colossus")
    void opponentCannotTargetWithSpells() {
        Permanent flora = addFloraReady(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, flora.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addFloraReady(Player player) {
        Permanent permanent = new Permanent(new FloraColossus());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
