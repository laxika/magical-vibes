package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlagstonesOfTrokair.class, Plains.class, GrizzlyBears.class})
class FlagstonesOfTrokairTest extends BaseCardTest {

    @Test
    void tapsForWhiteMana() {
        harness.addToBattlefield(player1, new FlagstonesOfTrokair());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void acceptsGraveyardTriggerToPutAPlainsOntoTheBattlefieldTapped() {
        Permanent flagstones = harness.addToBattlefieldAndReturn(player1, new FlagstonesOfTrokair());
        Plains plains = new Plains();
        List<Card> library = List.of(plains, new GrizzlyBears());
        harness.setLibrary(player1, library);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, flagstones));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plains);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == plains && permanent.isTapped());
    }

    @Test
    void decliningGraveyardTriggerDoesNotSearch() {
        Permanent flagstones = harness.addToBattlefieldAndReturn(player1, new FlagstonesOfTrokair());
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, flagstones));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard() == plains);
    }
}
