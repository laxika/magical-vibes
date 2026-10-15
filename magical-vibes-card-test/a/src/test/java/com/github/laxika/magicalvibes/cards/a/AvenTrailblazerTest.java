package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenTrailblazer.class, Plains.class, Island.class, Swamp.class, Mountain.class, Forest.class})
class AvenTrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("Toughness equals the number of basic land types you control; power stays 2")
    void toughnessEqualsBasicLandTypes() {
        Permanent trailblazer = addReady(player1);
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Duplicate basic land types count only once")
    void duplicateTypesCountOnce() {
        Permanent trailblazer = addReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only your lands, not opponent lands")
    void countsOnlyControllersLands() {
        Permanent trailblazer = addReady(player1);
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(1);
    }

    @Test
    @DisplayName("Toughness updates when basic land types change")
    void toughnessUpdatesWhenLandsChange() {
        Permanent trailblazer = addReady(player1);
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(1);

        harness.addToBattlefield(player1, new Mountain());
        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Mountain"));
        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(2);
    }

    @Test
    @DisplayName("With no lands, zero toughness sends Trailblazer to the graveyard")
    void diesWithoutBasicLandTypes() {
        Permanent trailblazer = addReady(player1);

        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isZero();
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(trailblazer);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(trailblazer.getCard());
    }

    @Test
    @DisplayName("All five basic land types give five toughness")
    void countsAllFiveBasicLandTypes() {
        Permanent trailblazer = addReady(player1);
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        harness.runStateBasedActions();

        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(trailblazer);
    }

    private Permanent addReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new AvenTrailblazer());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
