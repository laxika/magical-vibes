package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageLooseLeafTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Page adds {C}")
    void tapsForColorlessMana() {
        Permanent page = harness.addToBattlefieldAndReturn(player1, new PageLooseLeaf());
        page.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Grandeur discards another Page and puts the first instant or sorcery into hand")
    void grandeurFindsInstantOrSorcery() {
        PageLooseLeaf discardedPage = new PageLooseLeaf();
        Forest forest = new Forest();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new PageLooseLeaf());
        harness.setHand(player1, List.of(discardedPage, new GrizzlyBears()));
        harness.setLibrary(player1, List.of(forest, shock, bears));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedPage);
        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, bears);
    }

    @Test
    @DisplayName("Grandeur cannot be activated without another Page")
    void grandeurRequiresAnotherNamedCard() {
        harness.addToBattlefield(player1, new PageLooseLeaf());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
