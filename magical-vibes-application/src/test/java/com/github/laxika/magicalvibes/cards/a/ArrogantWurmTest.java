package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArrogantWurm.class, RavensCrime.class})
class ArrogantWurmTest extends BaseCardTest {

    private ArrogantWurm discardViaRavensCrime() {
        ArrogantWurm wurm = new ArrogantWurm();
        harness.setHand(player1, List.of(wurm));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return wurm;
    }

    @Test
    @DisplayName("Discarding Arrogant Wurm exiles it and offers madness cast")
    void discardTriggersMadness() {
        ArrogantWurm wurm = discardViaRavensCrime();

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
        ArrogantWurm wurm = discardViaRavensCrime();
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
        ArrogantWurm wurm = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(wurm.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
