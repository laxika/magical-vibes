package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrenkosBuzzcrusher.class, GhostQuarter.class, Forest.class, Island.class})
class KrenkosBuzzcrusherTest extends BaseCardTest {

    @Test
    @DisplayName("The controller chooses one nonbasic land per player and destroyed lands are replaced")
    void choosesAndReplacesDestroyedLands() {
        Permanent ownChosen = harness.addToBattlefieldAndReturn(player1, new GhostQuarter());
        Permanent ownSurvivor = harness.addToBattlefieldAndReturn(player1, new GhostQuarter());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());
        setLibrary(player1, new Forest());
        setLibrary(player2, new Island());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of(ownChosen.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentLand.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player1.getId());

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player2.getId());
        harness.getGameService().handleInteractionAnswer(gd, player2,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownSurvivor);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownChosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentLand);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest") && permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Island") && permanent.isTapped());
    }

    @Test
    @DisplayName("The up-to-one choices can all be declined")
    void canDeclineEveryLandChoice() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addToBattlefield(player2, new GhostQuarter());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Ghost Quarter"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Ghost Quarter"));
    }

    private void cast() {
        harness.setHand(player1, List.of(new KrenkosBuzzcrusher()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Player player, Card... cards) {
        harness.setLibrary(player, List.of(cards));
    }
}
