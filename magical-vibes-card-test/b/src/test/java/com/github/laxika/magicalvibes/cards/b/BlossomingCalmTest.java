package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlossomingCalm.class, Shock.class})
class BlossomingCalmTest extends BaseCardTest {

    @Test
    void gainsLifeAndHexproofUntilYourNextTurn() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BlossomingCalm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 12);
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void reboundOffersOneFreeCastAtNextUpkeep() {
        harness.setLife(player1, 10);
        BlossomingCalm card = new BlossomingCalm();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Blossoming Calm");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void hexproofExpiresAtTheBeginningOfYourNextTurn() {
        harness.setHand(player1, List.of(new BlossomingCalm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();

        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        declareAttackers(player1, List.of());
        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        declareAttackers(player2, List.of());
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
    }

    @Test
    void decliningReboundLeavesTheCardExiledWithoutAnotherOffer() {
        harness.setLife(player1, 10);
        BlossomingCalm card = new BlossomingCalm();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 12);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertLife(player1, 12);
    }

    @Test
    void hexproofAllowsSpellsControlledByTheProtectedPlayerToTargetThem() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BlossomingCalm(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 10);
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void gainingHexproofInResponseMakesAnOpponentsPlayerTargetIllegal() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BlossomingCalm()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isFalse();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reboundWaitsForYourUpkeepAndHexproofEndsBeforeTheReboundSpellResolves() {
        harness.setLife(player1, 10);
        BlossomingCalm card = new BlossomingCalm();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        declareAttackers(player1, List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        declareAttackers(player2, List.of());
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
        harness.assertLife(player1, 12);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        harness.assertLife(player1, 14);
    }
}
