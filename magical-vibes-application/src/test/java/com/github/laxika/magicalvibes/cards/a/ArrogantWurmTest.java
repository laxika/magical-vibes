package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.u.Unhinge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArrogantWurm.class, Unhinge.class})
class ArrogantWurmTest extends BaseCardTest {

    private ArrogantWurm discardViaUnhinge() {
        ArrogantWurm wurm = new ArrogantWurm();
        harness.setHand(player1, List.of(wurm));
        harness.setHand(player2, List.of(new Unhinge()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return wurm;
    }

    @Test
    @DisplayName("Discarding Arrogant Wurm exiles it and offers madness cast")
    void discardTriggersMadness() {
        ArrogantWurm wurm = discardViaUnhinge();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getDescription()).contains("madness");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining madness cast puts Arrogant Wurm into the graveyard")
    void decliningMadnessGoesToGraveyard() {
        ArrogantWurm wurm = discardViaUnhinge();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(wurm.getId()));
    }

    @Test
    @DisplayName("Accepting madness cast pays {2}{G} and puts Arrogant Wurm onto the battlefield")
    void acceptingMadnessCastsCreature() {
        ArrogantWurm wurm = discardViaUnhinge();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(wurm.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ArrogantWurm());
        Permanent blocker = addCreatureReady(player2, new AvenTrooper());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 3
        ));

        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player2, "Aven Trooper");
        harness.assertOnBattlefield(player1, "Arrogant Wurm");
    }
}
