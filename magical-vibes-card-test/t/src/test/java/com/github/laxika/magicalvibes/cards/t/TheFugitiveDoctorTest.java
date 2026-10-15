package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Preordain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheFugitiveDoctor.class, Shock.class, GrizzlyBears.class, Preordain.class})
class TheFugitiveDoctorTest extends BaseCardTest {

    @Test
    void entersAndInvestigates() {
        castDoctor();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void attackingMaySacrificeClueToGrantFixedCostFlashback() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(sacrificeChoice.validIds()).containsExactly(findPermanents(player1, "Clue").getFirst().getId());
        harness.handlePermanentChosen(player1, sacrificeChoice.validIds().iterator().next());

        PendingInteraction.MultiGraveyardChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(targetChoice.validCardIds()).containsExactly(shock.getId());
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackCostsUntilEndOfTurn).containsEntry(shock.getId(), "{2}{R}{G}");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void decliningAttackTriggerKeepsClue() {
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void maySacrificeClueEvenWithoutGraveyardTargets() {
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);
        harness.setGraveyard(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Clue").getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotGrantFlashbackWithoutAClue() {
        Preordain preordain = new Preordain();
        harness.setGraveyard(player1, List.of(preordain));
        addCreatureReady(player1, new TheFugitiveDoctor());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(preordain.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetsOnlyOwnInstantOrSorceryCardsAfterSacrificing() {
        Preordain preordain = new Preordain();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Preordain opposingPreordain = new Preordain();
        harness.setGraveyard(player1, List.of(preordain, shock, bears));
        harness.setGraveyard(player2, List.of(opposingPreordain));
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Clue").getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(preordain.getId(), shock.getId());
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.handleMultipleCardsChosen(player1, List.of(preordain.getId()));
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackCostsUntilEndOfTurn)
                .containsEntry(preordain.getId(), "{2}{R}{G}")
                .doesNotContainKeys(shock.getId(), bears.getId(), opposingPreordain.getId());
    }

    @Test
    void targetLeavingGraveyardInResponseDoesNotGainFlashback() {
        Preordain preordain = new Preordain();
        harness.setGraveyard(player1, List.of(preordain));
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Clue").getId());
        harness.handleMultipleCardsChosen(player1, List.of(preordain.getId()));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(preordain.getId());

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(preordain.getId());
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashbackSpellIsExiledAfterResolving() {
        Shock shock = new Shock();
        grantFlashback(shock);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(shock.getId()));
    }

    @Test
    void flashbackPermissionExpiresAfterTheTurn() {
        Shock shock = new Shock();
        grantFlashback(shock);
        harness.setLibrary(player2, List.of(new TheFugitiveDoctor()));
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(shock.getId());
        assertThat(gd.cardsGrantedFlashbackCostsUntilEndOfTurn).doesNotContainKey(shock.getId());
    }

    private void grantFlashback(Shock shock) {
        harness.setGraveyard(player1, List.of(shock));
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Clue").getId());
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
    }

    private Permanent castDoctor() {
        harness.setHand(player1, List.of(new TheFugitiveDoctor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "The Fugitive Doctor");
    }
}
