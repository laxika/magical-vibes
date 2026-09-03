package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Reparations.class, DarkRitual.class, Disenchant.class, GiantMantis.class, Incinerate.class})
class ReparationsTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent spell targeting you triggers the may ability and drawing adds a card")
    void opponentSpellTargetingYouDraws() {
        harness.addToBattlefield(player1, new Reparations());
        setUpOpponentTurn();

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Opponent spell targeting a creature you control triggers the may ability")
    void opponentSpellTargetingYourCreatureDraws() {
        harness.addToBattlefield(player1, new Reparations());
        harness.addToBattlefield(player1, new GiantMantis());
        setUpOpponentTurn();

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        UUID mantisId = harness.getPermanentId(player1, "Giant Mantis");
        harness.castInstant(player2, 0, mantisId);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the may ability draws nothing")
    void decliningDrawsNothing() {
        harness.addToBattlefield(player1, new Reparations());
        setUpOpponentTurn();

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Opponent spell targeting their own creature does not trigger")
    void opponentSpellTargetingOwnCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new Reparations());
        harness.addToBattlefield(player2, new GiantMantis());
        setUpOpponentTurn();

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        UUID mantisId = harness.getPermanentId(player2, "Giant Mantis");
        harness.castInstant(player2, 0, mantisId);

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Your own spell targeting you does not trigger")
    void ownSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Reparations());

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Opponent spell targeting a noncreature permanent you control does not trigger")
    void opponentSpellTargetingYourNoncreaturePermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new Reparations());
        setUpOpponentTurn();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        UUID reparationsId = harness.getPermanentId(player1, "Reparations");
        harness.castInstant(player2, 0, reparationsId);

        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Reparations");
    }

    @Test
    @DisplayName("Opponent spell with no targets does not trigger")
    void opponentNonTargetingSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Reparations());
        setUpOpponentTurn();

        harness.castFromHand(player2, new DarkRitual(), "{B}");

        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.passBothPriorities();
    }

    private void setUpOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
