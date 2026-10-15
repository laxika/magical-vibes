package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArcticMerfolk;
import com.github.laxika.magicalvibes.cards.d.DromarsCavern;
import com.github.laxika.magicalvibes.cards.q.QuirionExplorer;
import com.github.laxika.magicalvibes.cards.m.MoggSentry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShivanWurm.class, QuirionExplorer.class, MoggSentry.class,
        ArcticMerfolk.class, DromarsCavern.class})
class ShivanWurmTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a red or green creature you control, including itself")
    void etbOffersRedOrGreenCreaturesYouControl() {
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new QuirionExplorer()).getId();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new MoggSentry()).getId();
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new ArcticMerfolk()).getId();
        UUID opponentRedId = harness.addToBattlefieldAndReturn(player2, new MoggSentry()).getId();
        harness.addToBattlefield(player1, new DromarsCavern());

        castShivanWurm();
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        UUID wurmId = harness.getPermanentId(player1, "Shivan Wurm");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);

        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(greenId, redId, wurmId);
        assertThat(choice.validIds()).doesNotContain(blueId, opponentRedId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("ETB returns the chosen red or green creature to its owner's hand")
    void chosenCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new QuirionExplorer());
        UUID redId = harness.addToBattlefieldAndReturn(player1, new MoggSentry()).getId();
        harness.addToBattlefield(player1, new ArcticMerfolk());

        castShivanWurm();
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, redId);

        harness.assertInHand(player1, "Mogg Sentry");
        harness.assertOnBattlefield(player1, "Quirion Explorer");
        harness.assertOnBattlefield(player1, "Arctic Merfolk");
        harness.assertOnBattlefield(player1, "Shivan Wurm");
    }

    @Test
    @DisplayName("ETB must return itself when no other red or green creature is controlled")
    void returnsItselfWhenOnlyEligibleCreature() {
        harness.addToBattlefield(player1, new ArcticMerfolk());

        castShivanWurm();
        resolveAllTriggers();

        UUID wurmId = harness.getPermanentId(player1, "Shivan Wurm");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(wurmId);
        harness.handlePermanentChosen(player1, wurmId);

        harness.assertInHand(player1, "Shivan Wurm");
        harness.assertNotOnBattlefield(player1, "Shivan Wurm");
        harness.assertOnBattlefield(player1, "Arctic Merfolk");
    }

    @Test
    @DisplayName("ETB may return itself even when another eligible creature is controlled")
    void canChooseItselfOverAnotherCreature() {
        harness.addToBattlefield(player1, new QuirionExplorer());

        castShivanWurm();
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Shivan Wurm"));

        harness.assertInHand(player1, "Shivan Wurm");
        harness.assertNotOnBattlefield(player1, "Shivan Wurm");
        harness.assertOnBattlefield(player1, "Quirion Explorer");
    }

    @Test
    @DisplayName("ETB returns an opponent-owned creature you control to that opponent's hand")
    void returnsControlledCreatureToItsOwner() {
        QuirionExplorer explorer = new QuirionExplorer();
        explorer.setOwnerId(player2.getId());
        UUID explorerId = harness.addToBattlefieldAndReturn(player1, explorer).getId();

        castShivanWurm();
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, explorerId);

        harness.assertInHand(player2, "Quirion Explorer");
        harness.assertNotInHand(player1, "Quirion Explorer");
        harness.assertNotOnBattlefield(player1, "Quirion Explorer");
        harness.assertOnBattlefield(player1, "Shivan Wurm");
    }

    private void castShivanWurm() {
        harness.setHand(player1, List.of(new ShivanWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
    }
}
