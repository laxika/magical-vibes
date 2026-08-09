package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElvishClancallerTest extends BaseCardTest {

    @Test
    @DisplayName("Other Elves you control get +1/+1")
    void buffsOtherElvesYouControl() {
        harness.addToBattlefield(player1, new ElvishClancaller());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Elvish Clancaller does not buff itself, non-Elves, or opposing Elves")
    void onlyBuffsOtherOwnElves() {
        Permanent clancaller = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, clancaller)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, clancaller)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability searches for an Elvish Clancaller and puts it onto the battlefield")
    void searchesNamedCardToBattlefield() {
        Permanent clancaller = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        clancaller.setSummoningSick(false);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new ElvishClancaller()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).allMatch(card -> card.getName().equals("Elvish Clancaller"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> permanent.getCard().getName().equals("Elvish Clancaller"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
