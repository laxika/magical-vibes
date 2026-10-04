package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChandraPyrogenius;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiberatingCombustion.class, ChandraPyrogenius.class, Forest.class, GrizzlyBears.class})
class LiberatingCombustionTest extends BaseCardTest {

    @Test
    void dealsSixDamageAndSearchesLibraryForChandra() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card chandra = new ChandraPyrogenius();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(chandra, forest));

        cast(target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(chandra.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chandra.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chandra);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void searchesGraveyardForChandra() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card chandra = new ChandraPyrogenius();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(chandra));
        harness.setLibrary(player1, List.of(forest));

        cast(target.getId());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(chandra.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chandra.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chandra);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(chandra);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void mayDeclineSearchingForChandra() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card chandra = new ChandraPyrogenius();
        harness.setLibrary(player1, List.of(chandra));

        cast(target.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(chandra);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chandra);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LiberatingCombustion()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new LiberatingCombustion()));
        addMana();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
