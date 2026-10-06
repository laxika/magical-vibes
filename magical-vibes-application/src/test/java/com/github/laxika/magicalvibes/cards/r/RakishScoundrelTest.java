package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakishScoundrel.class, GrizzlyBears.class, Shock.class})
class RakishScoundrelTest extends BaseCardTest {

    @Test
    void enteringGivesIndestructibleToTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RakishScoundrel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void turningFaceUpGivesIndestructibleToTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent scoundrel = castFaceDown();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scoundrel));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void faceDownWardCountersAnOpponentSpellUnlessTheyPayTwo() {
        Permanent scoundrel = castFaceDown();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, scoundrel.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(scoundrel.isFaceDown()).isTrue();
    }

    @Test
    void faceDownEntryDoesNotTriggerIndestructible() {
        Permanent scoundrel = castFaceDown();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void payingWardAllowsOpponentSpellToResolve() {
        Permanent scoundrel = castFaceDown();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, scoundrel.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scoundrel);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof RakishScoundrel);
    }

    @Test
    void enteringCanProtectItselfAndProtectionExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new RakishScoundrel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent scoundrel = findPermanent(player1, "Rakish Scoundrel");

        harness.handlePermanentChosen(player1, scoundrel.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void turningFaceUpCanProtectItselfUsingOnlyGreenForHybridCost() {
        Permanent scoundrel = castFaceDown();
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scoundrel));
        harness.handlePermanentChosen(player1, scoundrel.getId());
        resolveAllTriggers();

        assertThat(scoundrel.isFaceDown()).isFalse();
        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new RakishScoundrel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Rakish Scoundrel");
    }
}
