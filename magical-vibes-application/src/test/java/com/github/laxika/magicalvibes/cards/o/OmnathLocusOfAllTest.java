package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BorborygmosAndFblthp;
import com.github.laxika.magicalvibes.cards.r.RealmbreakerTheInvasionTree;
import com.github.laxika.magicalvibes.cards.t.TributeToTheWorldTree;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.u.UlalekFusedAtrocity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmnathLocusOfAll.class, RealmbreakerTheInvasionTree.class,
        BorborygmosAndFblthp.class, TributeToTheWorldTree.class, TurnToFrog.class,
        UlalekFusedAtrocity.class})
class OmnathLocusOfAllTest extends BaseCardTest {

    @Test
    @DisplayName("The controller's unspent mana becomes black instead of draining")
    void unspentManaBecomesBlackInsteadOfDraining() {
        addOmnath();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("An ineligible top card goes to hand without a reveal")
    void putsIneligibleTopCardIntoHand() {
        Card topCard = new RealmbreakerTheInvasionTree();
        harness.setLibrary(player1, List.of(topCard));
        addOmnath();

        resolveOmnathTrigger();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The controller may decline revealing an eligible top card")
    void mayDeclineRevealingEligibleTopCard() {
        Card topCard = new OmnathLocusOfAll();
        harness.setLibrary(player1, List.of(topCard));
        addOmnath();

        resolveOmnathTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Revealing an eligible top card adds three mana in its colors")
    void revealsEligibleTopCardAndAddsManaInItsColors() {
        Card topCard = new BorborygmosAndFblthp();
        harness.setLibrary(player1, List.of(topCard));
        addOmnath();

        resolveOmnathTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsFirstMainPhase() {
        Card topCard = new RealmbreakerTheInvasionTree();
        harness.setLibrary(player1, List.of(topCard));
        addOmnath();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void threeSymbolsOfOneColorQualifyForThreeMana() {
        Card topCard = new TributeToTheWorldTree();
        harness.setLibrary(player1, List.of(topCard));
        addOmnath();

        resolveOmnathTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void mayChooseTheSameColorForAllThreeMana() {
        Card topCard = new BorborygmosAndFblthp();
        harness.setLibrary(player1, List.of(topCard));
        addOmnath();

        resolveOmnathTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void revealingDevoidCardAddsNoMana() {
        Card topCard = new UlalekFusedAtrocity();
        harness.setLibrary(player1, List.of(topCard));
        addOmnath();

        resolveOmnathTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotCauseDrawingFromEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        addOmnath();

        resolveOmnathTrigger();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    void losingAbilitiesStopsManaRetention() {
        addOmnath();
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Omnath, Locus of All"));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void losingAbilitiesStopsMainPhaseTrigger() {
        addOmnath();
        Card topCard = new RealmbreakerTheInvasionTree();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Omnath, Locus of All"));

        resolveOmnathTrigger();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    private void addOmnath() {
        harness.addToBattlefield(player1, new OmnathLocusOfAll());
    }

    private void resolveOmnathTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();
    }

}
