package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CyclopeanGiant.class, Forest.class, GrizzlyBears.class, Shock.class})
class CyclopeanGiantTest extends BaseCardTest {

    @Test
    void deathTriggerTurnsTargetLandIntoSwampAndExilesGiant() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CyclopeanGiant());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID giantId = giant.getId();
        UUID forestId = forest.getId();
        UUID giantCardId = giant.getCard().getId();

        harness.castInstant(player2, 0, giantId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forestId);
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(giantCardId));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(giantCardId));
    }

    @Test
    void deathTriggerOnlyOffersLandsAsTargets() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CyclopeanGiant());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(forest.getId());
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
