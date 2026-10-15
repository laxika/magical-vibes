package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LothoCorruptShirriff.class, Shock.class, DarkRitual.class})
class LothoCorruptShirriffTest extends BaseCardTest {

    @Test
    void losesLifeAndCreatesTreasureWhenControllerCastsSecondSpell() {
        addCreatureReady(player1, new LothoCorruptShirriff());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void triggersWhenAnOpponentCastsTheirSecondSpell() {
        addCreatureReady(player1, new LothoCorruptShirriff());
        harness.setHand(player2, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0);
        harness.castAndResolveInstant(player2, 0);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void countsEachPlayersSpellsIndependentlyInTheSameTurn() {
        addCreatureReady(player1, new LothoCorruptShirriff());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.setHand(player2, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0);

        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0);
        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.passBothPriorities();

        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        harness.passBothPriorities();
    }

    @Test
    void countsLothoAsTheFirstSpellOfTheTurn() {
        harness.setHand(player1, List.of(new LothoCorruptShirriff(), new DarkRitual()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.castInstant(player1, 0);
        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void doesNotTriggerWhenLothoItselfIsTheSecondSpell() {
        harness.setHand(player1, List.of(new DarkRitual(), new LothoCorruptShirriff(), new DarkRitual()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void triggeredAbilityResolvesAfterLothoIsKilledInResponse() {
        addCreatureReady(player1, new LothoCorruptShirriff());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0);
        harness.castInstant(player1, 0);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Lotho, Corrupt Shirriff"));
        harness.assertInGraveyard(player1, "Lotho, Corrupt Shirriff");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.passBothPriorities();
    }
}
