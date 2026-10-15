package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DiregrafColossus;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.l.LightningAxe;
import com.github.laxika.magicalvibes.cards.p.PulseOfMurasa;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RelentlessDead.class, DiregrafColossus.class, QuilledWolf.class, FieryTemper.class,
        LightningAxe.class, PulseOfMurasa.class})
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
        prepareDeath(List.of(new DiregrafColossus(), new QuilledWolf()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        resolveUntilXTrigger(3);

        harness.assertOnBattlefield(player1, "Diregraf Colossus");
        harness.assertInGraveyard(player1, "Quilled Wolf");
        harness.assertInGraveyard(player1, "Relentless Dead");
        resolveRemainingTriggerByDecliningHandReturn();
    }

    @Test
    @DisplayName("The pay-X ability excludes Relentless Dead and non-Zombie cards")
    void reanimationExcludesSourceAndNonZombies() {
        prepareDeath(List.of(new QuilledWolf()));
        assertThat(gd.stack).hasSize(1);
        resolveUntilHandTrigger();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Quilled Wolf");
        harness.assertInGraveyard(player1, "Quilled Wolf");
        harness.assertInGraveyard(player1, "Relentless Dead");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The reanimation target is announced before either death trigger resolves")
    void reanimationTargetIsChosenBeforeResolution() {
        Card zombie = new DiregrafColossus();
        prepareDeath(List.of(zombie));

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        if (choice != null) {
            harness.handleGraveyardCardChosen(player1, choice.validIndices().getFirst());
        }

        assertThat(gd.stack).anySatisfy(entry ->
                assertThat(entry.getTargetId()).isEqualTo(zombie.getId()));
        harness.assertInGraveyard(player1, "Diregraf Colossus");
    }

    @Test
    @DisplayName("Declining the black payment leaves Relentless Dead in the graveyard")
    void decliningBlackPaymentLeavesItInGraveyard() {
        prepareDeath(List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        resolveUntilHandTrigger();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Relentless Dead");
        harness.assertNotInHand(player1, "Relentless Dead");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        resolveRemainingTriggerByDecliningX();
    }

    @Test
    @DisplayName("An old death trigger cannot return a card that left and reentered the graveyard")
    void oldDeathTriggerDoesNotReturnNewGraveyardObject() {
        addCreatureReady(player1, new QuilledWolf());
        prepareDeath(List.of());
        Card dead = gd.playerGraveyards.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new PulseOfMurasa(), new LightningAxe()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, dead.getId());
        harness.assertInHand(player1, "Relentless Dead");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstantWithDiscard(player1, 0,
                harness.getPermanentId(player1, "Quilled Wolf"), 1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Relentless Dead");

        harness.addMana(player1, ManaColor.BLACK, 1);
        resolveUntilHandTrigger();
        harness.handleMayAbilityChosen(player1, true);
        resolveRemainingTriggerByDecliningX();

        harness.assertInGraveyard(player1, "Relentless Dead");
        harness.assertNotInHand(player1, "Relentless Dead");
    }

    private void prepareDeath(List<Card> graveyardCards) {
        addCreatureReady(player1, new RelentlessDead());
        harness.setGraveyard(player1, graveyardCards);
        Permanent dead = findPermanent(player1, "Relentless Dead");
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, dead.getId());
    }

    private void resolveUntilHandTrigger() {
        for (int attempts = 0; attempts < 4; attempts++) {
            harness.passBothPriorities();
            PendingInteraction interaction = gd.interaction.activeInteraction();
            if (interaction instanceof PendingInteraction.MayAbilityChoice) {
                return;
            }
            if (interaction instanceof PendingInteraction.XValueChoice) {
                harness.handleXValueChosen(player1, 0);
            }
        }
        throw new AssertionError("Expected the return-to-hand payment choice");
    }

    private void resolveUntilXTrigger(int x) {
        for (int attempts = 0; attempts < 4; attempts++) {
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
        throw new AssertionError("Expected the reanimation payment choice");
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
