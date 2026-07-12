package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DroveOfElvesTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as a green permanent when alone: 1/1")
    void countsItselfWhenAlone() {
        Permanent drove = addDrove(player1);

        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals the number of green permanents you control")
    void ptEqualsGreenPermanentCount() {
        Permanent drove = addDrove(player1);
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());

        // itself + Llanowar Elves + Grizzly Bears = 3
        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-green permanents you control are not counted")
    void ignoresNonGreenPermanents() {
        Permanent drove = addDrove(player1);
        harness.addToBattlefield(player1, new SuntailHawk()); // white

        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts only your green permanents, not the opponent's")
    void ignoresOpponentGreenPermanents() {
        Permanent drove = addDrove(player1);
        harness.addToBattlefield(player2, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when green permanents change")
    void ptUpdatesWhenGreenPermanentsChange() {
        Permanent drove = addDrove(player1);
        harness.addToBattlefield(player1, new LlanowarElves());
        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(1);
    }

    private Permanent addDrove(Player player) {
        Permanent permanent = new Permanent(new DroveOfElves());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
