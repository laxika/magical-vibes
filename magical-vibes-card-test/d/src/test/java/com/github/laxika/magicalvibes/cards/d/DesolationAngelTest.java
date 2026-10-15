package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CavesOfKoilos;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DesolationAngel.class, CavesOfKoilos.class})
class DesolationAngelTest extends BaseCardTest {

    @Test
    void withoutKickerDestroysOnlyLandsYouControl() {
        harness.addToBattlefield(player1, new CavesOfKoilos());
        harness.addToBattlefield(player2, new CavesOfKoilos());
        harness.setHand(player1, List.of(new DesolationAngel()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Caves of Koilos");
        harness.assertOnBattlefield(player2, "Caves of Koilos");
    }

    @Test
    void whenKickedDestroysAllLands() {
        harness.addToBattlefield(player1, new CavesOfKoilos());
        harness.addToBattlefield(player2, new CavesOfKoilos());
        harness.setHand(player1, List.of(new DesolationAngel()));
        addBaseMana();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Caves of Koilos");
        harness.assertNotOnBattlefield(player2, "Caves of Koilos");
    }

    @Test
    void withoutKickerLeavesNonlandPermanentsUntouched() {
        addLandsAndNonlands();
        harness.setHand(player1, List.of(new DesolationAngel()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Desolation Angel");
        harness.assertOnBattlefield(player2, "Desolation Angel");
    }

    @Test
    void whenKickedLeavesNonlandPermanentsUntouched() {
        addLandsAndNonlands();
        harness.setHand(player1, List.of(new DesolationAngel()));
        addBaseMana();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Desolation Angel");
        harness.assertOnBattlefield(player2, "Desolation Angel");
    }

    @Test
    void withoutKickerDestroysLandsAtTriggerResolution() {
        harness.addToBattlefield(player1, new CavesOfKoilos());
        harness.addToBattlefield(player2, new CavesOfKoilos());
        harness.setHand(player1, List.of(new DesolationAngel()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Desolation Angel");
        harness.assertOnBattlefield(player1, "Caves of Koilos");
        harness.assertOnBattlefield(player2, "Caves of Koilos");
        harness.addToBattlefield(player1, new CavesOfKoilos());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Caves of Koilos");
        harness.assertInGraveyard(player1, "Caves of Koilos");
        harness.assertOnBattlefield(player2, "Caves of Koilos");
        harness.assertNotInGraveyard(player2, "Caves of Koilos");
    }

    @Test
    void whenKickedWithNoOwnLandsDestroysOpponentLandsAtTriggerResolution() {
        harness.setHand(player1, List.of(new DesolationAngel()));
        addBaseMana();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Desolation Angel");
        harness.addToBattlefield(player2, new CavesOfKoilos());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Caves of Koilos");
        harness.assertInGraveyard(player2, "Caves of Koilos");
        harness.assertOnBattlefield(player1, "Desolation Angel");
    }

    private void addLandsAndNonlands() {
        harness.addToBattlefield(player1, new CavesOfKoilos());
        harness.addToBattlefield(player2, new CavesOfKoilos());
        harness.addToBattlefield(player1, new DesolationAngel());
        harness.addToBattlefield(player2, new DesolationAngel());
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
