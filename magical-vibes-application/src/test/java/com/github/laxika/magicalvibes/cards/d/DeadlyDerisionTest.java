package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyDerision.class, GrizzlyBears.class, JaceBeleren.class, Plains.class})
class DeadlyDerisionTest extends BaseCardTest {

    @Test
    void destroysCreatureAndCreatesTreasure() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castDeadlyDerision(player1, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void destroysPlaneswalkerAndCreatesTreasure() {
        Permanent jace = addReadyJace(player2);

        castDeadlyDerision(player1, jace.getId());

        harness.assertInGraveyard(player2, "Jace Beleren");
        harness.assertNotOnBattlefield(player2, "Jace Beleren");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void cannotTargetNonCreatureNonPlaneswalker() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new DeadlyDerision()));
        addDeadlyDerisionMana(player1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Plains")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    private void castDeadlyDerision(Player player, java.util.UUID targetId) {
        harness.setHand(player, List.of(new DeadlyDerision()));
        addDeadlyDerisionMana(player);
        harness.castInstant(player, 0, targetId);
        harness.passBothPriorities();
    }

    private void addDeadlyDerisionMana(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyJace(Player player) {
        Permanent jace = new Permanent(new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        jace.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(jace);
        return jace;
    }
}
