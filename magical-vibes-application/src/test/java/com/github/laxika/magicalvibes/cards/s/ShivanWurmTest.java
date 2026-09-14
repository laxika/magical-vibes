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

@CardUsed({ShivanWurm.class, QuirionExplorer.class, MoggSentry.class, SaprazzanRaider.class,
        ArcticMerfolk.class, DromarsCavern.class})
class ShivanWurmTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a red or green creature you control, including itself")
    void etbOffersRedOrGreenCreaturesYouControl() {
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new QuirionExplorer()).getId();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new MoggSentry()).getId();
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new SaprazzanRaider()).getId();
        UUID opponentRedId = harness.addToBattlefieldAndReturn(player2, new MoggSentry()).getId();
        harness.addToBattlefield(player1, new DromarsCavern());

        castShivanWurm();
        resolveUntilPermanentChoice();

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
        harness.addToBattlefield(player1, new SaprazzanRaider());

        castShivanWurm();
        resolveUntilPermanentChoice();
        harness.handlePermanentChosen(player1, redId);

        harness.assertInHand(player1, "Mogg Sentry");
        harness.assertOnBattlefield(player1, "Quirion Explorer");
        harness.assertOnBattlefield(player1, "Saprazzan Raider");
        harness.assertOnBattlefield(player1, "Shivan Wurm");
    }

    private void castShivanWurm() {
        harness.setHand(player1, List.of(new ShivanWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
    }

    private void resolveUntilPermanentChoice() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
