package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxOfClearSkies.class, Forest.class, Island.class, GiantGrowth.class, Shock.class})
class SphinxOfClearSkiesTest extends BaseCardTest {

    @Test
    void combatDamageRevealsCardsBasedOnDomainAndPutsOnePileInHand() {
        Card giantGrowth = new GiantGrowth();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(giantGrowth, shock));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        Permanent sphinx = addCreatureReady(player1, new SphinxOfClearSkies());
        sphinx.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(giantGrowth.getId(), shock.getId());

        harness.handleMultipleCardsChosen(player2, List.of(giantGrowth.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(giantGrowth);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    void wardCountersAnOpponentSpellUnlessTheyPayTwoMana() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfClearSkies());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, sphinx.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(sphinx.getMarkedDamage()).isZero();
    }
}
