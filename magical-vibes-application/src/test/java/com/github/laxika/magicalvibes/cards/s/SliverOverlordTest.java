package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoatOfArms;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SliverOverlord.class, MetallicSliver.class, CoatOfArms.class})
class SliverOverlordTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability searches for a Sliver and puts it into hand")
    void searchesForSliver() {
        addCreatureReady(player1, new SliverOverlord());
        MetallicSliver sliver = new MetallicSliver();
        CoatOfArms coatOfArms = new CoatOfArms();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(sliver, coatOfArms));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(sliver);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(sliver);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(coatOfArms);
    }

    @Test
    @DisplayName("The first ability may fail to find a Sliver")
    void mayFailToFindSliver() {
        addCreatureReady(player1, new SliverOverlord());
        MetallicSliver sliver = new MetallicSliver();
        CoatOfArms coatOfArms = new CoatOfArms();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(sliver, coatOfArms));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(sliver);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(sliver, coatOfArms);
    }

    @Test
    @DisplayName("The second ability permanently gains control of a target Sliver")
    void gainsPermanentControlOfSliver() {
        addCreatureReady(player1, new SliverOverlord());
        Permanent sliver = addCreatureReady(player2, new MetallicSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, sliver.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sliver);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sliver);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sliver);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sliver);
    }

    @Test
    @DisplayName("The second ability cannot target a non-Sliver")
    void cannotTargetNonSliver() {
        addCreatureReady(player1, new SliverOverlord());
        Permanent coatOfArms = harness.addToBattlefieldAndReturn(player2, new CoatOfArms());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, coatOfArms.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
    }

}
