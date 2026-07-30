package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NectarFaerie;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WillowPriestessTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: puts a Faerie permanent card from hand onto the battlefield")
    void putsFaerieFromHandOntoBattlefield() {
        addCreatureReady(player1, new WillowPriestess());
        harness.setHand(player1, List.of(new NectarFaerie()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Nectar Faerie");
        harness.assertNotInHand(player1, "Nectar Faerie");
    }

    @Test
    @DisplayName("Declining the may leaves the Faerie card in hand")
    void decliningLeavesFaerieInHand() {
        addCreatureReady(player1, new WillowPriestess());
        harness.setHand(player1, List.of(new NectarFaerie()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Nectar Faerie");
        harness.assertInHand(player1, "Nectar Faerie");
    }

    @Test
    @DisplayName("{2}{G}: target green creature gains protection from black until end of turn")
    void grantsProtectionFromBlack() {
        addCreatureReady(player1, new WillowPriestess());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, bearsId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
    }

    @Test
    @DisplayName("The granted protection makes the creature an illegal target for a black spell")
    void protectionStopsBlackRemoval() {
        addCreatureReady(player1, new WillowPriestess());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, bearsId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that isn't green")
    void cannotTargetNonGreenCreature() {
        addCreatureReady(player1, new WillowPriestess());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, giantId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a green creature");
    }
}
