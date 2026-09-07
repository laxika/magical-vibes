package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CutShort.class, GrizzlyBears.class, JaceBeleren.class})
class CutShortTest extends BaseCardTest {

    @Test
    void destroysTappedCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        castCutShort(player1, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void destroysPlaneswalkerActivatedThisTurn() {
        Permanent jace = addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new CutShort()));
        addCutShortMana(player1);

        harness.castInstant(player1, 0, jace.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jace Beleren");
        harness.assertInGraveyard(player1, "Jace Beleren");
    }

    @Test
    void cannotTargetUnactivatedPlaneswalkerOrUntappedCreature() {
        Permanent jace = addReadyJace(player2);
        harness.setHand(player1, List.of(new CutShort()));
        addCutShortMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, jace.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCutShort(Player player, java.util.UUID targetId) {
        harness.setHand(player, List.of(new CutShort()));
        addCutShortMana(player);
        harness.castInstant(player, 0, targetId);
        harness.passBothPriorities();
    }

    private void addCutShortMana(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyJace(Player player) {
        Permanent jace = new Permanent(new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        jace.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(jace);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return jace;
    }
}
