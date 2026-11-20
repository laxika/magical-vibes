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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.effect.GrantedCardAbilityEffect;

@CardUsed({TheFugitiveDoctor.class, Shock.class, GrizzlyBears.class, Preordain.class})
class TheFugitiveDoctorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering investigates")
    void entersAndInvestigates() {
        castDoctor();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking may sacrifice a Clue to grant flashback with a fixed cost")
    void attackingMaySacrificeClueToGrantFixedCostFlashback() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Clue")).isEmpty();

        PendingInteraction.MultiGraveyardChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(targetChoice.validCardIds()).containsExactly(shock.getId());
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.grantedFlashbackCost(shock.getId())).isEqualTo("{2}{R}{G}");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the attack trigger keeps the Clue")
    void decliningAttackTriggerKeepsClue() {
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("A Clue may be sacrificed even with no graveyard target")
    void maySacrificeClueEvenWithoutGraveyardTargets() {
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);
        harness.setGraveyard(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Without a Clue no flashback is granted")
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

        assertThat(grantedCards(GrantedCardAbilityEffect.Ability.FLASHBACK)).doesNotContain(preordain.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targets only instant or sorcery cards in your own graveyard")
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

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(preordain.getId(), shock.getId());
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.handleMultipleCardsChosen(player1, List.of(preordain.getId()));
        harness.passBothPriorities();

        assertThat(gd.grantedFlashbackCost(preordain.getId())).isEqualTo("{2}{R}{G}");
        assertThat(gd.grantedFlashbackCost(shock.getId())).isNull();
        assertThat(gd.grantedFlashbackCost(bears.getId())).isNull();
        assertThat(gd.grantedFlashbackCost(opposingPreordain.getId())).isNull();
    }

    @Test
    @DisplayName("A target that leaves the graveyard in response gains no flashback")
    void targetLeavingGraveyardInResponseDoesNotGainFlashback() {
        Preordain preordain = new Preordain();
        harness.setGraveyard(player1, List.of(preordain));
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(preordain.getId()));
        assertThat(gd.stack).hasSize(1);
        assertThat(grantedCards(GrantedCardAbilityEffect.Ability.FLASHBACK)).doesNotContain(preordain.getId());

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(grantedCards(GrantedCardAbilityEffect.Ability.FLASHBACK)).doesNotContain(preordain.getId());
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A spell cast with the granted flashback is exiled after resolving")
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
    @DisplayName("The granted flashback expires after the turn")
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
        assertThat(grantedCards(GrantedCardAbilityEffect.Ability.FLASHBACK)).doesNotContain(shock.getId());
        assertThat(gd.grantedFlashbackCost(shock.getId())).isNull();
    }

    private void grantFlashback(Shock shock) {
        harness.setGraveyard(player1, List.of(shock));
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
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
