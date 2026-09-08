package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToHandReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RienneAngelOfRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other multicolored creatures you control")
    void boostsOtherMulticoloredCreatures() {
        harness.addToBattlefield(player1, new RienneAngelOfRebirth());
        harness.addToBattlefield(player1, new WoollyThoctar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent rienne = findPermanent(player1, "Rienne, Angel of Rebirth");
        Permanent woollyThoctar = findPermanent(player1, "Woolly Thoctar");
        Permanent grizzlyBears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, rienne)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, woollyThoctar)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, woollyThoctar)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, grizzlyBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns another multicolored creature to its owner's hand at the next end step")
    void returnsMulticoloredCreatureAtNextEndStep() {
        harness.addToBattlefield(player1, new RienneAngelOfRebirth());
        harness.addToBattlefield(player1, new WoollyThoctar());
        UUID woollyThoctarId = harness.getPermanentId(player1, "Woolly Thoctar");

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, woollyThoctarId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Woolly Thoctar");
        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        harness.assertInHand(player1, "Woolly Thoctar");
        harness.assertNotInGraveyard(player1, "Woolly Thoctar");
    }

    @Test
    @DisplayName("Does not return a monocolored creature")
    void doesNotReturnMonocoloredCreature() {
        harness.addToBattlefield(player1, new RienneAngelOfRebirth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID grizzlyBearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, grizzlyBearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).isEmpty();
    }
}
