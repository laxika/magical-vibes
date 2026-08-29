package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelentlessDeadTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, paying {B} returns Relentless Dead to its owner's hand")
    void payingBlackReturnsItToHand() {
        prepareDeath(List.of());

        resolveUntilHandTrigger();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Relentless Dead");
        resolveRemainingTriggerByDecliningX();
    }

    @Test
    @DisplayName("When it dies, paying X returns another matching Zombie immediately")
    void payingXReturnsAnotherZombieImmediately() {
        prepareDeath(List.of(new Gravecrawler(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        resolveUntilXTrigger(1);

        assertThat(findPermanent(player1, "Gravecrawler")).isNotNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Relentless Dead");
        resolveRemainingTriggerByDecliningHandReturn();
    }

    @Test
    @DisplayName("The pay-X ability excludes Relentless Dead and non-Zombie cards")
    void payXOnlyOffersAnotherZombieWithExactManaValue() {
        prepareDeath(List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        resolveUntilXTrigger(1);

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Relentless Dead");
        resolveRemainingTriggerByDecliningHandReturn();
    }

    private void prepareDeath(List<Card> graveyardCards) {
        addCreatureReady(player1, new RelentlessDead());
        harness.setGraveyard(player1, graveyardCards);
        Permanent dead = findPermanent(player1, "Relentless Dead");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, dead.getId());
        harness.passBothPriorities();
    }

    private void resolveUntilHandTrigger() {
        while (true) {
            harness.passBothPriorities();
            PendingInteraction interaction = gd.interaction.activeInteraction();
            if (interaction instanceof PendingInteraction.MayAbilityChoice) {
                return;
            }
            if (interaction instanceof PendingInteraction.XValueChoice) {
                harness.handleXValueChosen(player1, 0);
            }
        }
    }

    private void resolveUntilXTrigger(int x) {
        while (true) {
            harness.passBothPriorities();
            PendingInteraction interaction = gd.interaction.activeInteraction();
            if (interaction instanceof PendingInteraction.XValueChoice) {
                harness.handleXValueChosen(player1, x);
                PendingInteraction.GraveyardChoice choice =
                        gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
                if (choice != null) {
                    harness.handleGraveyardCardChosen(player1, choice.validIndices().getFirst());
                }
                return;
            }
            if (interaction instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, false);
            }
        }
    }

    private void resolveRemainingTriggerByDecliningX() {
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.XValueChoice) {
            harness.handleXValueChosen(player1, 0);
        }
    }

    private void resolveRemainingTriggerByDecliningHandReturn() {
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }
    }
}
