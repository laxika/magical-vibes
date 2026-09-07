package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeliodsIntervention.class, Ornithopter.class, GloriousAnthem.class, GrizzlyBears.class})
class HeliodsInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroy mode destroys exactly X target artifacts and/or enchantments")
    void destroysExactlyXArtifactsAndEnchantments() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playModalXCard(gd, player1, 0, 0, 2, null, List.of(artifact.getId(), enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroy mode requires exactly X targets")
    void destroyModeRequiresExactlyXTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> gs.playModalXCard(
                gd, player1, 0, 0, 2, null, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroy mode rejects a nonartifact nonenchantment permanent")
    void destroyModeRejectsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> gs.playModalXCard(
                gd, player1, 0, 0, 1, null, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Life mode makes the target player gain twice X life")
    void targetPlayerGainsTwiceXLife() {
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castModalInstantForX(player1, 0, 1, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
