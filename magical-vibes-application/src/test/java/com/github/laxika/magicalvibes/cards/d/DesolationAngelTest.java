package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CavesOfKoilos;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof CavesOfKoilos);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof CavesOfKoilos);
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof CavesOfKoilos);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof CavesOfKoilos);
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
