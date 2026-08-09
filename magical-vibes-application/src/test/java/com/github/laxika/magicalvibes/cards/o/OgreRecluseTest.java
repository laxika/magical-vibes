package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OgreRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casting a spell taps Ogre Recluse")
    void controllerCastingSpellTapsOgreRecluse() {
        Permanent recluse = addReadyRecluse(player1);
        castGrizzlyBears(player1);

        assertThat(recluse.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent casting a spell taps Ogre Recluse")
    void opponentCastingSpellTapsOgreRecluse() {
        Permanent recluse = addReadyRecluse(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castGrizzlyBears(player2);

        assertThat(recluse.isTapped()).isTrue();
    }

    private Permanent addReadyRecluse(Player player) {
        return addCreatureReady(player, new OgreRecluse());
    }

    private void castGrizzlyBears(Player player) {
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
    }
}
