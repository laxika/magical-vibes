package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NicolBolasTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
    }

    @Test
    @DisplayName("Declining to pay {U}{B}{R} sacrifices Nicol Bolas")
    void declineSacrifices() {
        harness.addToBattlefield(player1, new NicolBolas());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nicol Bolas"));
    }

    @Test
    @DisplayName("Paying {U}{B}{R} keeps Nicol Bolas and spends the mana")
    void payKeeps() {
        harness.addToBattlefield(player1, new NicolBolas());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nicol Bolas"));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Dealing combat damage to an opponent makes them discard their whole hand")
    void combatDamageDiscardsHand() {
        harness.setLife(player2, 20);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        Permanent bolas = new Permanent(new NicolBolas());
        bolas.setSummoningSick(false);
        bolas.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(bolas);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        // Pass through combat damage and the resulting triggered ability resolution.
        harness.passBothPriorities();
        harness.passBothPriorities();

        // The whole hand (both cards) was discarded to the graveyard.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(2)
                .allMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
