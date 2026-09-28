package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MahadiEmporiumMaster.class, GrizzlyBears.class, Shock.class})
class MahadiEmporiumMasterTest extends BaseCardTest {

    @Test
    void createsTreasureForEachCreatureThatDiedThisTurn() {
        harness.addToBattlefield(player1, new MahadiEmporiumMaster());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, ownCreature.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, opposingCreature.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void doesNotCreateTreasureWhenNoCreatureDiedThisTurn() {
        harness.addToBattlefield(player1, new MahadiEmporiumMaster());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
