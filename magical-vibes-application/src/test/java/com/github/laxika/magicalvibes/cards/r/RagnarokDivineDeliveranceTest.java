package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FangFearlessLCie;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VanilleCheerfulLCie;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RagnarokDivineDeliverance.class, Forest.class, FangFearlessLCie.class,
        VanilleCheerfulLCie.class, RelmsSketching.class})
class RagnarokDivineDeliveranceTest extends BaseCardTest {

    @Test
    void cannotPutDeathAbilityOnStackWithoutNonlegendaryGraveyardTarget() {
        harness.setGraveyard(player1, List.of(new FangFearlessLCie(), new VanilleCheerfulLCie()));
        Permanent ragnarok = harness.addToBattlefieldAndReturn(player1, new RagnarokDivineDeliverance());
        harness.addToBattlefield(player2, new Forest());

        kill(ragnarok);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void graveyardTargetExcludesLegendaryCardsAndOpponentsCards() {
        Forest returned = new Forest();
        Forest opponentsCard = new Forest();
        harness.setGraveyard(player1, List.of(returned, new FangFearlessLCie(), new VanilleCheerfulLCie(),
                new RelmsSketching()));
        harness.setGraveyard(player2, List.of(opponentsCard));
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ragnarok = harness.addToBattlefieldAndReturn(player1, new RagnarokDivineDeliverance());

        kill(ragnarok);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, destroyed.getId());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(returned.getId());
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentsCard, destroyed.getCard());
    }

    @Test
    void returnsGraveyardTargetWhenPermanentTargetLeavesBeforeResolution() {
        Forest returned = new Forest();
        harness.setGraveyard(player1, List.of(returned));
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ragnarok = harness.addToBattlefieldAndReturn(player1, new RagnarokDivineDeliverance());

        kill(ragnarok);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, destroyed.getId());
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, destroyed));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).contains(destroyed.getCard());
    }

    private void kill(Permanent ragnarok) {
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ragnarok));
        harness.runStateBasedActions();
    }
}
