package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuandrixApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant offers a land from the top three and bottoms the rest")
    void castingInstantTriggersLandSelection() {
        addCreatureReady(player1, new QuandrixApprentice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Plains plains = new Plains();
        Shock topNonland = new Shock();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(plains, topNonland, forest));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains, forest);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topNonland, forest);
    }

    @Test
    @DisplayName("Copying an instant triggers Quandrix Apprentice's magecraft")
    void copyingInstantTriggersMagecraft() {
        addCreatureReady(player1, new QuandrixApprentice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireA = addCreatureReady(player1, new QuandrixApprentice());
        Permanent conspireB = addCreatureReady(player1, new QuandrixApprentice());
        harness.setLibrary(player1, List.of(
                new Plains(), new Forest(), new Island(), new Plains(), new Forest(), new Island()));
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }
}
