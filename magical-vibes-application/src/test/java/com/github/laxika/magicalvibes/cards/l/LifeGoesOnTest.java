package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.f.FirebrandArcher;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifeGoesOn.class, Abrade.class, FirebrandArcher.class, Unsummon.class})
class LifeGoesOnTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 4 life when no creature died this turn")
    void gains4LifeWithoutMorbid() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LifeGoesOn()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Gains 8 life when a creature died this turn")
    void gains8LifeWithMorbid() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LifeGoesOn()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(28);
    }

    @Test
    @DisplayName("Morbid is checked at resolution time")
    void morbidCheckedAtResolution() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LifeGoesOn()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);

        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(28);
    }

    @Test
    @DisplayName("Killing a creature with Abrade enables morbid")
    void actualCreatureDeathEnablesMorbid() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Abrade(), new LifeGoesOn()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player2, new FirebrandArcher());

        UUID archerId = harness.getPermanentId(player2, "Firebrand Archer");
        harness.castInstant(player1, 0, 0, archerId);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(28);
    }

    @Test
    @DisplayName("A creature dying in response upgrades life gain at resolution")
    void creatureDiesInResponse() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LifeGoesOn(), new Abrade()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addToBattlefield(player2, new FirebrandArcher());

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Firebrand Archer"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Firebrand Archer");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 28);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Multiple creature deaths still grant only 8 life")
    void multipleDeathsDoNotMultiplyLifeGain() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Abrade(), new Abrade(), new LifeGoesOn()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player2, new FirebrandArcher());
        harness.addToBattlefield(player2, new FirebrandArcher());

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Firebrand Archer"));
        harness.passBothPriorities();
        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Firebrand Archer"));
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("Returning a creature to hand does not enable morbid")
    void returningCreatureToHandDoesNotCountAsDeath() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Unsummon(), new LifeGoesOn()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player2, new FirebrandArcher());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Firebrand Archer"));
        harness.assertInHand(player2, "Firebrand Archer");
        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 24);
    }
}
