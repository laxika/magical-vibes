package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Deathgazer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrowdOfCindersTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as a black permanent when alone: 1/1")
    void countsItselfWhenAlone() {
        Permanent crowd = addCrowd(player1);

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals the number of black permanents you control")
    void ptEqualsBlackPermanents() {
        Permanent crowd = addCrowd(player1);
        harness.addToBattlefield(player1, new Deathgazer());
        harness.addToBattlefield(player1, new Deathgazer());

        // itself + 2 black creatures = 3
        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-black permanents are not counted")
    void nonBlackNotCounted() {
        Permanent crowd = addCrowd(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only counts your black permanents, not the opponent's")
    void countsOnlyControllersPermanents() {
        Permanent crowd = addCrowd(player1);
        harness.addToBattlefield(player2, new Deathgazer());

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when black permanents change")
    void ptUpdatesWhenBlackPermanentsChange() {
        Permanent crowd = addCrowd(player1);
        harness.addToBattlefield(player1, new Deathgazer());
        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Deathgazer"));
        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(1);
    }

    private Permanent addCrowd(Player player) {
        Permanent permanent = new Permanent(new CrowdOfCinders());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
