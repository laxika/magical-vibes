package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.cards.c.CarrionRats;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FacelessButcher.class, CabalCoffers.class, CarrionRats.class, FieryTemper.class})
class FacelessButcherTest extends BaseCardTest {

    private void castAndExileTarget(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FacelessButcher(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles another target creature")
    void etbExilesAnotherTargetCreature() {
        harness.addToBattlefield(player2, new CarrionRats());
        UUID targetId = harness.getPermanentId(player2, "Carrion Rats");

        castAndExileTarget(targetId);

        harness.assertNotOnBattlefield(player2, "Carrion Rats");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Carrion Rats"));
    }

    @Test
    @DisplayName("Another creature can be chosen under its controller's control")
    void canChooseOwnAnotherCreature() {
        harness.addToBattlefield(player1, new CarrionRats());
        UUID targetId = harness.getPermanentId(player1, "Carrion Rats");

        castAndExileTarget(targetId);

        harness.assertNotOnBattlefield(player1, "Carrion Rats");
    }

    @Test
    @DisplayName("Exiled creature returns when Faceless Butcher leaves")
    void exiledCreatureReturnsWhenButcherDies() {
        harness.addToBattlefield(player2, new CarrionRats());
        UUID targetId = harness.getPermanentId(player2, "Carrion Rats");
        castAndExileTarget(targetId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        UUID butcherId = harness.getPermanentId(player1, "Faceless Butcher");
        harness.castAndResolveInstant(player2, 0, butcherId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Faceless Butcher");
        harness.assertOnBattlefield(player2, "Carrion Rats");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Carrion Rats"));
    }

    @Test
    @DisplayName("If Faceless Butcher leaves before its ETB resolves, the creature remains exiled")
    void leavingBeforeEtbResolutionLeavesCreatureExiled() {
        harness.addToBattlefield(player2, new CarrionRats());
        UUID targetId = harness.getPermanentId(player2, "Carrion Rats");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FacelessButcher(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        UUID butcherId = harness.getPermanentId(player1, "Faceless Butcher");
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, butcherId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Carrion Rats");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Carrion Rats"));
    }

    @Test
    @DisplayName("ETB does not target a noncreature permanent")
    void etbDoesNotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new CabalCoffers());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FacelessButcher(), "{2}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Faceless Butcher");
        harness.assertOnBattlefield(player2, "Cabal Coffers");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Cabal Coffers"));
    }
}
