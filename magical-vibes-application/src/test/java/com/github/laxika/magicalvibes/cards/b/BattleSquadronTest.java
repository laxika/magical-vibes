package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BattleSquadronTest extends BaseCardTest {

    @Test
    @DisplayName("Battle Squadron is 1/1 when it is your only creature")
    void isOneOneWhenOnlyCreature() {
        Permanent squadron = addBattleSquadronReady(player1);

        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(1);
    }

    @Test
    @DisplayName("Battle Squadron power and toughness equal creatures you control")
    void ptEqualsControlledCreatures() {
        Permanent squadron = addBattleSquadronReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(3);
    }

    @Test
    @DisplayName("Battle Squadron counts only its controller's creatures")
    void countsOnlyControllersCreatures() {
        Permanent squadron = addBattleSquadronReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(1);
    }

    @Test
    @DisplayName("Battle Squadron updates as creatures enter and leave")
    void ptUpdatesAsCreaturesChange() {
        Permanent squadron = addBattleSquadronReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(1);
    }

    private Permanent addBattleSquadronReady(Player player) {
        BattleSquadron card = new BattleSquadron();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
