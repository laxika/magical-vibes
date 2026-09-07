package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AxebaneFerox.class, GiantGrowth.class, GrizzlyBears.class, Shock.class})
class AxebaneFeroxTest extends BaseCardTest {

    @Test
    void wardCountersSpellWhenControllerCannotCollectEvidence() {
        Permanent ferox = harness.addToBattlefieldAndReturn(player1, new AxebaneFerox());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, ferox.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
    }

    @Test
    void decliningToCollectEvidenceCountersSpell() {
        Permanent ferox = harness.addToBattlefieldAndReturn(player1, new AxebaneFerox());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(first, second));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, ferox.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second, shock);
    }

    @Test
    void collectingEvidenceLetsTargetedSpellResolveAndExilesChosenCards() {
        Permanent ferox = harness.addToBattlefieldAndReturn(player1, new AxebaneFerox());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        GiantGrowth spell = new GiantGrowth();
        harness.setGraveyard(player2, List.of(first, second));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, ferox.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gqs.getEffectivePower(gd, ferox)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, ferox)).isEqualTo(7);
    }
}
