package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AinokGuide;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vaultbreaker.class, Forest.class, AinokGuide.class})
class VaultbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking may discard a card and draw a card")
    void attackingMayDiscardAndDraw() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new AinokGuide()));
        addVaultbreaker();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Ainok Guide");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the discarded card in hand")
    void decliningAttackTriggerDoesNotDiscard() {
        harness.setHand(player1, List.of(new AinokGuide()));
        harness.setLibrary(player1, List.of(new Forest()));
        addVaultbreaker();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Ainok Guide");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Normal cast does not use dash")
    void normalCastDoesNotUseDash() {
        harness.setHand(player1, List.of(new Vaultbreaker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent vaultbreaker = findPermanent(player1, "Vaultbreaker");
        assertThat(vaultbreaker.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Vaultbreaker")).isSameAs(vaultbreaker);
    }

    @Test
    @DisplayName("Dash grants haste and returns Vaultbreaker at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new Vaultbreaker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        Permanent vaultbreaker = findPermanent(player1, "Vaultbreaker");
        assertThat(vaultbreaker.hasKeyword(Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Vaultbreaker");
        harness.assertNotOnBattlefield(player1, "Vaultbreaker");
    }

    @Test
    @DisplayName("Attacking with an empty hand cannot draw a card")
    void attackingWithEmptyHandDoesNotDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addVaultbreaker();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Dash does not create an enters-the-battlefield trigger")
    void dashDoesNotCreateAnEnterTrigger() {
        harness.setHand(player1, List.of(new Vaultbreaker()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vaultbreaker");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dash creates exactly one return trigger at the next end step")
    void dashCreatesOnlyOneReturnTrigger() {
        harness.setHand(player1, List.of(new Vaultbreaker()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Vaultbreaker");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertInHand(player1, "Vaultbreaker");
        harness.assertNotOnBattlefield(player1, "Vaultbreaker");
    }

    private void addVaultbreaker() {
        addCreatureReady(player1, new Vaultbreaker());
    }
}
