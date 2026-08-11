package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HootingMandrillsTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic creature cost")
    void delvePaysGenericCost() {
        List<Card> graveyard = List.of(
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new HootingMandrills()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HootingMandrills);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);

        Permanent mandrills = new Permanent(new HootingMandrills());
        mandrills.setSummoningSick(false);
        mandrills.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(mandrills);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                bears.getId(), 2,
                player2.getId(), 2
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Hooting Mandrills");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
