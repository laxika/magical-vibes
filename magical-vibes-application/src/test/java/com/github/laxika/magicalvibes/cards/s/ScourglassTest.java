package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScourglassTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys non-artifact non-land permanents and sacrifices Scourglass")
    void destroysNonArtifactNonLandPermanents() {
        Permanent scourglass = addScourglassReady(player1);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent icy = new Permanent(new IcyManipulator());
        gd.playerBattlefields.get(player1.getId()).add(icy);

        Permanent plains = new Permanent(new Plains());
        gd.playerBattlefields.get(player2.getId()).add(plains);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Creature destroyed
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Artifact survives
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(icy);
        // Land survives
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(plains);
        // Scourglass sacrificed as cost
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scourglass);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scourglass"));
    }

    @Test
    @DisplayName("Cannot activate during precombat main phase")
    void cannotActivateOutsideUpkeep() {
        addScourglassReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot activate during opponent's upkeep")
    void cannotActivateDuringOpponentUpkeep() {
        addScourglassReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Artifact creatures survive; enchantments are destroyed")
    void sparesArtifactCreaturesDestroysEnchantments() {
        addScourglassReady(player1);

        Permanent ornithopter = new Permanent(new Ornithopter());
        ornithopter.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ornithopter);

        Permanent pacifism = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(pacifism);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ornithopter);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(pacifism);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Pacifism"));
    }

    private Permanent addScourglassReady(Player player) {
        Permanent perm = new Permanent(new Scourglass());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
