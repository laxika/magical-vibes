package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameBlessedBolt.class, AirElemental.class, ChandraBoldPyromancer.class,
        GrizzlyBears.class, Shock.class, Forest.class})
class FlameBlessedBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a creature and exiles it instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");

        castAt(target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Kills a planeswalker and exiles it instead of putting it into the graveyard")
    void killsAndExilesPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        target.setCounterCount(CounterType.LOYALTY, 2);

        castAt(target.getId());

        harness.assertNotOnBattlefield(player2, "Chandra, Bold Pyromancer");
        harness.assertNotInGraveyard(player2, "Chandra, Bold Pyromancer");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Chandra, Bold Pyromancer");
    }

    @Test
    @DisplayName("Exiles a creature killed later in the turn after it survives the bolt")
    void exilesCreatureKilledLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castAt(target.getId());
        harness.assertOnBattlefield(player2, "Air Elemental");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a land")
    void rejectsLandTarget() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new FlameBlessedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FlameBlessedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
