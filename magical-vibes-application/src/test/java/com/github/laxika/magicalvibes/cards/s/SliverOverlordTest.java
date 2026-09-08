package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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
    @DisplayName("The first ability searches for a Sliver card and puts it into hand")
    void searchesForSliverCard() {
        harness.addToBattlefield(player1, new SliverOverlord());
        MetallicSliver sliver = new MetallicSliver();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(sliver, bears));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(sliver);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(sliver);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("The second ability gains permanent control of a target Sliver")
    void gainsPermanentControlOfTargetSliver() {
        harness.addToBattlefield(player1, new SliverOverlord());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MetallicSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("The second ability cannot target a non-Sliver permanent")
    void rejectsNonSliverTarget() {
        harness.addToBattlefield(player1, new SliverOverlord());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Sliver");
    }
}
