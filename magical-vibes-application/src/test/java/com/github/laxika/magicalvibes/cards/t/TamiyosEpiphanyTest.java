package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TamiyosEpiphany.class, Forest.class, Island.class, Mountain.class, Plains.class,
        GrizzlyBears.class, LlanowarElves.class})
class TamiyosEpiphanyTest extends BaseCardTest {

    @Test
    @DisplayName("Scries four, then draws two cards")
    void scriesFourThenDrawsTwo() {
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card plains = new Plains();
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(forest, island, mountain, plains, bears, elves));
        castTamiyosEpiphany();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(forest, island, mountain, plains);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0, 2, 3)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island, bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elves, forest, mountain, plains);
    }

    @Test
    @DisplayName("An empty library produces no draw")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        castTamiyosEpiphany();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castTamiyosEpiphany() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TamiyosEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }
}
