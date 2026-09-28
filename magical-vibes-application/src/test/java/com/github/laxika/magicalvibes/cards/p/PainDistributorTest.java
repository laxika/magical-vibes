package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PainDistributor.class, Shock.class, DarkRitual.class, MindStone.class, Naturalize.class})
class PainDistributorTest extends BaseCardTest {

    @Test
    void createsTreasureForEachPlayersFirstSpell() {
        addCreatureReady(player1, new PainDistributor());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void dealsDamageToTheControllerOfAnOpponentsArtifact() {
        addCreatureReady(player1, new PainDistributor());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, mindStoneId);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void doesNotTriggerForAnArtifactControlledByItsController() {
        addCreatureReady(player1, new PainDistributor());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLife(player1, 20);

        UUID mindStoneId = harness.getPermanentId(player1, "Mind Stone");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, mindStoneId);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
