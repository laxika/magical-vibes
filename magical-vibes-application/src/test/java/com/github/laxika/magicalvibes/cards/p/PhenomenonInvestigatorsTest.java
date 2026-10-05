package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhenomenonInvestigators.class, GrizzlyBears.class, Shock.class, Island.class})
class PhenomenonInvestigatorsTest extends BaseCardTest {

    @Test
    @DisplayName("Believe creates a Horror when a nontoken creature you control dies")
    void believeCreatesHorrorOnNontokenCreatureDeath() {
        castAndChoose("Believe");
        harness.addToBattlefield(player1, new GrizzlyBears());

        killCreature(player1, player2);

        assertThat(findPermanents(player1, "Horror")).hasSize(1);
    }

    @Test
    @DisplayName("Doubt may return an owned nonland permanent and draw a card")
    void doubtReturnsOwnedNonlandPermanentAndDraws() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        castAndChoose("Doubt");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears.getCard(), drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Doubt does nothing when its optional return is declined")
    void doubtCanBeDeclined() {
        castAndChoose("Doubt");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void believeCreatesHorrorWhenInvestigatorsDie() {
        castAndChoose("Believe");
        Permanent investigators = findPermanent(player1, "Phenomenon Investigators");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, investigators));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Phenomenon Investigators");
        assertThat(findPermanents(player1, "Horror")).hasSize(1);
    }

    @Test
    void believeCreatesEnchantmentCreatureAndIgnoresItsTokenDeath() {
        castAndChoose("Believe");
        harness.addToBattlefield(player1, new GrizzlyBears());
        killCreature(player1, player2);
        Permanent horror = findPermanent(player1, "Horror");
        assertThat(horror.getCard().isToken()).isTrue();
        assertThat(horror.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(horror.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
        assertThat(horror.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(horror.getCard().getSubtypes()).containsExactly(CardSubtype.HORROR);
        assertThat(horror.getEffectivePower()).isEqualTo(2);
        assertThat(horror.getEffectiveToughness()).isEqualTo(2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, horror));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Horror")).isEmpty();
    }

    @Test
    void believeIgnoresOpponentsCreatureDeath() {
        castAndChoose("Believe");
        harness.addToBattlefield(player2, new GrizzlyBears());
        killCreature(player2, player1);

        assertThat(findPermanents(player1, "Horror")).isEmpty();
    }

    @Test
    void doubtDoesNotCreateHorrors() {
        castAndChoose("Doubt");
        harness.addToBattlefield(player1, new GrizzlyBears());
        killCreature(player1, player2);

        assertThat(findPermanents(player1, "Horror")).isEmpty();
    }

    @Test
    void believeHasNoEndStepAbility() {
        castAndChoose("Believe");
        advanceToEndStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doubtDoesNotTriggerAtOpponentsEndStep() {
        castAndChoose("Doubt");
        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doubtCanReturnInvestigatorsThemselves() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        castAndChoose("Doubt");
        Permanent investigators = findPermanent(player1, "Phenomenon Investigators");
        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, investigators.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(investigators.getCard(), drawnCard);
        harness.assertNotOnBattlefield(player1, "Phenomenon Investigators");
    }

    @Test
    void doubtCanReturnOwnedPermanentControlledByOpponent() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        castAndChoose("Doubt");
        GrizzlyBears bearsCard = new GrizzlyBears();
        bearsCard.setOwnerId(player1.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, bearsCard);
        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerHands.get(player1.getId())).contains(bearsCard, drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(bearsCard);
    }

    @Test
    void doubtExcludesLandsAndPermanentsOwnedByOpponent() {
        castAndChoose("Doubt");
        Permanent investigators = findPermanent(player1, "Phenomenon Investigators");
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        GrizzlyBears borrowedCard = new GrizzlyBears();
        borrowedCard.setOwnerId(player2.getId());
        Permanent borrowed = harness.addToBattlefieldAndReturn(player1, borrowedCard);
        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(investigators.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(island.getId(), borrowed.getId());
    }

    private void castAndChoose(String mode) {
        harness.castFromHand(player1, new PhenomenonInvestigators(), "{2}{U}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }

    private void killCreature(com.github.laxika.magicalvibes.model.Player targetController,
                              com.github.laxika.magicalvibes.model.Player caster) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(targetController, "Grizzly Bears");
        harness.castAndResolveInstant(caster, 0, targetId);
        resolveAllTriggers();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
