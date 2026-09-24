package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainNghathrod.class, Forest.class, GrizzlyBears.class, MindStone.class})
class CaptainNghathrodTest extends BaseCardTest {

    @Test
    void givesHorrorsYouControlMenace() {
        Permanent captain = addCreatureReady(player1, new CaptainNghathrod());

        assertThat(gqs.hasKeyword(gd, captain, Keyword.MENACE)).isTrue();
    }

    @Test
    void horrorCombatDamageMillsThatManyCards() {
        Permanent captain = addCreatureReady(player1, new CaptainNghathrod());
        captain.setAttacking(true);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new MindStone(), new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void endStepReturnsOnlyAQualifyingCardPutIntoGraveyardFromLibrary() {
        Card eligible = new GrizzlyBears();
        Card ineligible = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(ineligible, eligible));
        gd.cardsPutIntoGraveyardFromLibraryThisTurn
                .computeIfAbsent(player2.getId(), ignored -> new HashSet<>())
                .add(eligible.getId());
        harness.addToBattlefield(player1, new CaptainNghathrod());

        advanceToEndStep(player1);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).containsExactly(eligible);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(eligible.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(ineligible);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
