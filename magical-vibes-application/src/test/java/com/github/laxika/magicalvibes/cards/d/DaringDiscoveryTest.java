package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaringDiscovery.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class, Plains.class})
class DaringDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents up to three target creatures from blocking")
    void preventsUpToThreeCreaturesFromBlocking() {
        var creature1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var creature2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var creature3 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Plains(), new HillGiant()));
        cast(List.of(creature1.getId(), creature2.getId(), creature3.getId()));

        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
        assertThat(creature3.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can be cast without choosing creatures and discovers a card into hand")
    void canChooseNoCreaturesAndPutDiscoveredCardIntoHand() {
        GrizzlyBears discovered = new GrizzlyBears();
        Plains land = new Plains();
        CrawWurm tooExpensive = new CrawWurm();
        harness.setLibrary(player1, List.of(land, tooExpensive, discovered));
        cast(List.of());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(land, tooExpensive);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Plains land = new Plains();
        harness.addToBattlefield(player2, land);
        harness.setHand(player1, List.of(new DaringDiscovery()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    private void cast(List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new DaringDiscovery()));
        addMana();
        harness.castSorcery(player1, 0, targets);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
