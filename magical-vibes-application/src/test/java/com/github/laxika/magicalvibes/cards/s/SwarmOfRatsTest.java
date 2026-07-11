package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwarmOfRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Swarm of Rats is 1/1 when it is the only Rat you control")
    void isOneOneWhenOnlyRat() {
        Permanent swarm = addSwarmReady(player1);

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Swarm of Rats power equals the number of Rats you control; toughness stays 1")
    void powerEqualsRatsYouControl() {
        Permanent swarm = addSwarmReady(player1);
        harness.addToBattlefield(player1, new RavenousRats());
        harness.addToBattlefield(player1, new RavenousRats());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Swarm of Rats counts only Rats, not other creatures you control")
    void countsOnlyRats() {
        Permanent swarm = addSwarmReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Swarm of Rats counts only your Rats, not opponent Rats")
    void countsOnlyControllersRats() {
        Permanent swarm = addSwarmReady(player1);
        harness.addToBattlefield(player2, new RavenousRats());
        harness.addToBattlefield(player2, new RavenousRats());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    private Permanent addSwarmReady(Player player) {
        Permanent permanent = new Permanent(new SwarmOfRats());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
