package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GixYawgmothPraetorTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from a creature you control may be paid for with 1 life to draw")
    void combatDamageMayBePaidForWithLifeToDraw() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        attacker.setAttacking(true);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        int lifeBefore = gd.getLife(player1.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Discarding X cards exiles X cards and permits free play from the source")
    void discardXExilesXAndPermitsFreePlay() {
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        Card exiled = new GrizzlyBears();
        Card secondExiled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiled, secondExiled));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(exiled, secondExiled);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
