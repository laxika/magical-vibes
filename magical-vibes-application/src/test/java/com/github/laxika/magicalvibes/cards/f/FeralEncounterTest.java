package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeralEncounter.class, GrizzlyBears.class, Forest.class})
class FeralEncounterTest extends BaseCardTest {

    @Test
    void castsWithoutTargetsAndBitesAtTheNextCombat() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card creatureToExile = new GrizzlyBears();
        harness.setLibrary(player1, List.of(
                creatureToExile, new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new FeralEncounter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(creatureToExile);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creatureToExile);
        assertThat(gd.exilePlayPermissions).containsEntry(creatureToExile.getId(), player1.getId());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, source.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
    }
}
