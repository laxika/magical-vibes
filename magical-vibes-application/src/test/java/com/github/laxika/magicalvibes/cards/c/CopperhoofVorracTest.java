package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CopperhoofVorracTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each untapped permanent an opponent controls")
    void countsUntappedOpponentPermanents() {
        Permanent vorrac = addVorrac(player1);
        addPermanent(player2, new GrizzlyBears());
        addPermanent(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, vorrac)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vorrac)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count tapped or controller's permanents")
    void ignoresTappedAndOwnPermanents() {
        Permanent vorrac = addVorrac(player1);
        addPermanent(player1, new GrizzlyBears());
        addPermanent(player2, new GrizzlyBears());
        Permanent tappedMountain = addPermanent(player2, new Mountain());
        tappedMountain.tap();

        assertThat(gqs.getEffectivePower(gd, vorrac)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vorrac)).isEqualTo(3);
    }

    @Test
    @DisplayName("Updates as an opponent's permanent becomes tapped or untapped")
    void updatesWithTapStateChanges() {
        Permanent vorrac = addVorrac(player1);
        Permanent opponentPermanent = addPermanent(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, vorrac)).isEqualTo(3);

        opponentPermanent.tap();
        assertThat(gqs.getEffectivePower(gd, vorrac)).isEqualTo(2);

        opponentPermanent.untap();
        assertThat(gqs.getEffectivePower(gd, vorrac)).isEqualTo(3);
    }

    private Permanent addVorrac(Player player) {
        Permanent permanent = new Permanent(new CopperhoofVorrac());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
