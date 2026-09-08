package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeastbondOutcaster.class, AirElemental.class, HillGiant.class, GrizzlyBears.class})
class BeastbondOutcasterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws a card when you control a creature with power 4 or greater")
    void drawsWithCreaturePowerFourOrGreater() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new BeastbondOutcaster()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addBeastbondOutcasterMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Beastbond Outcaster");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw when your creatures all have power less than 4")
    void doesNotDrawWithUnderpoweredCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new BeastbondOutcaster()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addBeastbondOutcasterMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when only an opponent controls a creature with power 4 or greater")
    void doesNotDrawWithOpponentCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new BeastbondOutcaster()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addBeastbondOutcasterMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addBeastbondOutcasterMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
