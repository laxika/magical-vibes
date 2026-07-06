package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EarthServantTest extends BaseCardTest {

    // ===== Base stats without Mountains =====

    @Test
    @DisplayName("Without Mountains, is 4/4")
    void withoutMountainsIs4x4() {
        Permanent earthServant = addEarthServant(player1);

        assertThat(gqs.getEffectivePower(gd, earthServant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, earthServant)).isEqualTo(4);
    }

    // ===== With one Mountain =====

    @Test
    @DisplayName("With one Mountain, is 4/5")
    void withOneMountainIs4x5() {
        Permanent earthServant = addEarthServant(player1);
        addMountain(player1);

        assertThat(gqs.getEffectivePower(gd, earthServant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, earthServant)).isEqualTo(5);
    }

    // ===== With multiple Mountains =====

    @Test
    @DisplayName("With three Mountains, is 4/7")
    void withThreeMountainsIs4x7() {
        Permanent earthServant = addEarthServant(player1);
        addMountain(player1);
        addMountain(player1);
        addMountain(player1);

        assertThat(gqs.getEffectivePower(gd, earthServant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, earthServant)).isEqualTo(7);
    }

    // ===== Opponent's Mountains don't count =====

    @Test
    @DisplayName("Opponent's Mountains don't affect Earth Servant's toughness")
    void opponentMountainsDontCount() {
        Permanent earthServant = addEarthServant(player1);
        addMountain(player2);
        addMountain(player2);

        assertThat(gqs.getEffectivePower(gd, earthServant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, earthServant)).isEqualTo(4);
    }

    // ===== Non-Mountain lands don't count =====

    @Test
    @DisplayName("Non-Mountain lands don't affect Earth Servant's toughness")
    void nonMountainLandsDontCount() {
        Permanent earthServant = addEarthServant(player1);
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Forest()));

        assertThat(gqs.getEffectivePower(gd, earthServant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, earthServant)).isEqualTo(4);
    }

    // ===== Helpers =====

    private Permanent addEarthServant(Player player) {
        Permanent perm = new Permanent(new EarthServant());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addMountain(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new Mountain()));
    }
}
