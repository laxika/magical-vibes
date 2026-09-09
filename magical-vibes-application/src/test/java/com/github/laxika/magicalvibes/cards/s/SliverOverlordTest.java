package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SliverOverlord.class, MetallicSliver.class, GrizzlyBears.class})
class SliverOverlordTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability searches for a Sliver and puts it into hand")
    void searchesForSliver() {
        addOverlord(player1);
        MetallicSliver sliver = new MetallicSliver();
        GrizzlyBears bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(sliver, bears));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(sliver);
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(sliver);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("The second ability permanently gains control of a target Sliver")
    void gainsPermanentControlOfSliver() {
        addOverlord(player1);
        Permanent sliver = addReadyPermanent(player2, new MetallicSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, sliver.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sliver);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sliver);
    }

    @Test
    @DisplayName("The second ability cannot target a non-Sliver")
    void cannotTargetNonSliver() {
        addOverlord(player1);
        Permanent bears = addReadyPermanent(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
    }

    private void addOverlord(com.github.laxika.magicalvibes.model.Player player) {
        Permanent overlord = harness.addToBattlefieldAndReturn(player, new SliverOverlord());
        overlord.setSummoningSick(false);
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player,
                                         com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
