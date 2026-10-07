package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HardEvidence;
import com.github.laxika.magicalvibes.cards.u.UnholyHeat;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpecimenCollector.class, UnholyHeat.class, HardEvidence.class})
class SpecimenCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a Squirrel and a Crab")
    void enteringCreatesTokens() {
        castSpecimenCollector();

        Permanent squirrel = findPermanent(player1, "Squirrel");
        Permanent crab = findPermanent(player1, "Crab");
        assertThat(squirrel.getCard().getSubtypes()).containsExactly(CardSubtype.SQUIRREL);
        assertThat(crab.getCard().getSubtypes()).containsExactly(CardSubtype.CRAB);
        assertThat(squirrel.getCard().getPower()).isEqualTo(1);
        assertThat(squirrel.getCard().getToughness()).isEqualTo(1);
        assertThat(crab.getCard().getPower()).isZero();
        assertThat(crab.getCard().getToughness()).isEqualTo(3);
        assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(crab.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(squirrel.getCard().isToken()).isTrue();
        assertThat(crab.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("When it dies, creates a copy of a target token you control")
    void deathCreatesTokenCopy() {
        castSpecimenCollector();
        Permanent squirrel = findPermanent(player1, "Squirrel");
        Permanent specimenCollector = findPermanent(player1, "Specimen Collector");

        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, specimenCollector.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(squirrel.getId(), findPermanent(player1, "Crab").getId());
        harness.handlePermanentChosen(player1, squirrel.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(2);
        assertThat(findPermanents(player1, "Crab")).hasSize(1);
    }

    @Test
    @DisplayName("The death trigger cannot target a nontoken or an opponent's token")
    void deathTriggerRequiresOwnToken() {
        castSpecimenCollector();
        Permanent specimenCollector = findPermanent(player1, "Specimen Collector");
        Permanent nontoken = harness.addToBattlefieldAndReturn(player1, new SpecimenCollector());
        harness.enterBattlefieldAndReturn(player2, new SpecimenCollector());
        resolveAllTriggers();
        Permanent opponentToken = findPermanent(player2, "Squirrel");

        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, specimenCollector.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(findPermanent(player1, "Squirrel").getId(), findPermanent(player1, "Crab").getId())
                .doesNotContain(opponentToken.getId(), nontoken.getId());
    }

    private void castSpecimenCollector() {
        harness.setHand(player1, List.of(new SpecimenCollector()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    @Test
    void deathCanCopyCrabWithoutCopyingTappedState() {
        castSpecimenCollector();
        Permanent crab = findPermanent(player1, "Crab");
        crab.setTapped(true);
        killCollector();
        harness.handlePermanentChosen(player1, crab.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Crab")).hasSize(2);
        Permanent copy = findPermanents(player1, "Crab").stream()
                .filter(p -> !p.getId().equals(crab.getId())).findFirst().orElseThrow();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCard().getPower()).isZero();
        assertThat(copy.getCard().getToughness()).isEqualTo(3);
        assertThat(copy.getCard().getColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    void deathCanCopyNoncreatureToken() {
        castSpecimenCollector();
        harness.setHand(player1, List.of(new HardEvidence()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent clue = findPermanent(player1, "Clue");
        killCollector();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(clue.getId());
        harness.handlePermanentChosen(player1, clue.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(2)
                .allSatisfy(p -> assertThat(p.getCard().getType()).isEqualTo(CardType.ARTIFACT));
    }

    @Test
    void deathDoesNotCopyTokenRemovedInResponse() {
        castSpecimenCollector();
        Permanent squirrel = findPermanent(player1, "Squirrel");
        killCollector();
        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handlePermanentChosen(player1, squirrel.getId());
        harness.castAndResolveInstant(player1, 0, squirrel.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Squirrel")).isEmpty();
        assertThat(findPermanents(player1, "Crab")).hasSize(1);
    }

    @Test
    void deathWithoutEligibleTokensDoesNotPromptOrCreateToken() {
        harness.addToBattlefield(player1, new SpecimenCollector());
        killCollector();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Specimen Collector");
    }

    private void killCollector() {
        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Specimen Collector").getId());
    }
}
