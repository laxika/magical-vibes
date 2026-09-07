package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SunscorchedDesert;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColossalRattlewurm.class, Forest.class, SunscorchedDesert.class})
class ColossalRattlewurmTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast at instant speed while its controller controls a Desert")
    void canBeCastAtInstantSpeedWithDesert() {
        harness.addToBattlefield(player1, new SunscorchedDesert());
        prepareInstantSpeedCast();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot be cast at instant speed without a Desert")
    void cannotBeCastAtInstantSpeedWithoutDesert() {
        prepareInstantSpeedCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Its graveyard ability exiles it and puts a Desert onto the battlefield tapped")
    void graveyardAbilityFetchesTappedDesert() {
        Card wurm = new ColossalRattlewurm();
        Card desert = new SunscorchedDesert();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(wurm));
        harness.setLibrary(player1, List.of(desert, forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(wurm.getId()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .extracting(Card::getId)
                .containsExactly(desert.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(desert.getId())
                        && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void prepareInstantSpeedCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ColossalRattlewurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passPriority(player2);
    }
}
