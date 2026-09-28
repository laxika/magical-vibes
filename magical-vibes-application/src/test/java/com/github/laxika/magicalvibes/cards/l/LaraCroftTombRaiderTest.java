package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GaeasCradle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaraCroftTombRaider.class, GaeasCradle.class, MoxAmber.class, GrizzlyBears.class})
class LaraCroftTombRaiderTest extends BaseCardTest {

    @Test
    void attackingExilesOptionalLegendaryArtifactOrLandWithDiscoveryCounter() {
        addReadyLara();
        Card validLand = new GaeasCradle();
        Card validArtifact = new MoxAmber();
        Card invalid = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(invalid)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(validLand, validArtifact)));

        declareAttackers(List.of(0));

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(validLand.getId(), validArtifact.getId());
        assertThat(choice.validCardIds()).doesNotContain(invalid.getId());

        harness.handleMultipleCardsChosen(player1, List.of(validLand.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(validLand);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(validLand);
        assertThat(gd.exiledCardsWithDiscoveryCounters).contains(validLand.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(validLand.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(validLand.getId());
    }

    @Test
    void raidCreatesTreasureAtEndOfCombat() {
        addReadyLara();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private Permanent addReadyLara() {
        Permanent lara = harness.addToBattlefieldAndReturn(player1, new LaraCroftTombRaider());
        lara.setSummoningSick(false);
        return lara;
    }
}
