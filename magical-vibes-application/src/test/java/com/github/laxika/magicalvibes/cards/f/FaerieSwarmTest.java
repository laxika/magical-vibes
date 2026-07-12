package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FaerieSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as a blue permanent when alone: 1/1")
    void countsItselfWhenAlone() {
        Permanent swarm = addSwarm(player1);

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals the number of blue permanents you control")
    void ptEqualsBluePermanents() {
        Permanent swarm = addSwarm(player1);
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new AirElemental());

        // itself + 2 blue creatures = 3
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-blue permanents are not counted")
    void nonBlueNotCounted() {
        Permanent swarm = addSwarm(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only counts your blue permanents, not the opponent's")
    void countsOnlyControllersPermanents() {
        Permanent swarm = addSwarm(player1);
        harness.addToBattlefield(player2, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when blue permanents change")
    void ptUpdatesWhenBluePermanentsChange() {
        Permanent swarm = addSwarm(player1);
        harness.addToBattlefield(player1, new AirElemental());
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Air Elemental"));
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    private Permanent addSwarm(Player player) {
        Permanent permanent = new Permanent(new FaerieSwarm());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
