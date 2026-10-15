package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SlipOutTheBack;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtractionSpecialist.class, GrizzlyBears.class, HillGiant.class, Shock.class,
        SlipOutTheBack.class, Threaten.class})
class ExtractionSpecialistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target creature with mana value 2 or less")
    void etbReturnsTargetCheapCreature() {
        GrizzlyBears bears = new GrizzlyBears();

        Permanent returned = castSpecialistAndReturn(bears);

        assertThat(returned.getCard().getId()).isEqualTo(bears.getId());
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB only offers creature cards with mana value 2 or less")
    void etbFiltersIllegalGraveyardCards() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(giant, bears));

        castSpecialist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
    }

    @Test
    @DisplayName("The returned creature cannot attack while its Specialist remains under your control")
    void returnedCreatureCannotAttackWhileSpecialistIsControlled() {
        Permanent returned = castSpecialistAndReturn(new GrizzlyBears());
        returned.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(indexOf(player1, returned))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The returned creature cannot block while its Specialist remains under your control")
    void returnedCreatureCannotBlockWhileSpecialistIsControlled() {
        Permanent returned = castSpecialistAndReturn(new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, returned), indexOf(player2, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("The restriction ends when the Specialist leaves the battlefield")
    void restrictionEndsWhenSpecialistLeaves() {
        Permanent returned = castSpecialistAndReturn(new GrizzlyBears());
        Permanent specialist = findPermanent(player1, "Extraction Specialist");
        returned.setSummoningSick(false);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, specialist.getId());

        harness.assertInGraveyard(player1, "Extraction Specialist");

        assertThatCode(() -> declareAttackers(player1, List.of(indexOf(player1, returned))))
                .doesNotThrowAnyException();
    }

    @Test
    void etbExcludesNoncreaturesAndOpponentsGraveyard() {
        GrizzlyBears ownCreature = new GrizzlyBears();
        GrizzlyBears opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new Shock(), ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));

        castSpecialist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void noLegalTargetDoesNotPreventSpecialistFromEntering() {
        harness.setGraveyard(player1, List.of(new HillGiant(), new Shock()));

        castSpecialist();

        harness.assertOnBattlefield(player1, "Extraction Specialist");
        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void specialistLeavingBeforeTriggerResolvesDoesNotRestrictReturnedCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castSpecialist();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        Permanent specialist = findPermanent(player1, "Extraction Specialist");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, specialist.getId());
        resolveAllTriggers();

        Permanent returned = findPermanentByCardId(player1, bears.getId());
        returned.setSummoningSick(false);
        harness.assertInGraveyard(player1, "Extraction Specialist");
        assertThatCode(() -> declareAttackers(player1, List.of(indexOf(player1, returned))))
                .doesNotThrowAnyException();
    }

    @Test
    void restrictionDoesNotResumeWhenControlOfSpecialistIsRegained() {
        Permanent returned = castSpecialistAndReturn(new GrizzlyBears());
        Permanent specialist = findPermanent(player1, "Extraction Specialist");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Threaten()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player2, 0, specialist.getId());
        harness.assertOnBattlefield(player2, "Extraction Specialist");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Extraction Specialist");
        returned.setSummoningSick(false);
        assertThatCode(() -> declareAttackers(player1, List.of(indexOf(player1, returned))))
                .doesNotThrowAnyException();
    }

    @Test
    void restrictionDoesNotResumeWhenSpecialistPhasesBackIn() {
        Permanent returned = castSpecialistAndReturn(new GrizzlyBears());
        Permanent specialist = findPermanent(player1, "Extraction Specialist");
        harness.setHand(player1, List.of(new SlipOutTheBack()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, specialist.getId());
        harness.assertNotOnBattlefield(player1, "Extraction Specialist");

        harness.performUntapStep(player1);
        harness.assertOnBattlefield(player1, "Extraction Specialist");
        returned.setSummoningSick(false);
        assertThatCode(() -> declareAttackers(player1, List.of(indexOf(player1, returned))))
                .doesNotThrowAnyException();
    }

    @Test
    void returnedCreatureRemainsRestrictedAfterItsControllerChanges() {
        Permanent returned = castSpecialistAndReturn(new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Threaten()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player2, 0, returned.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Extraction Specialist");

        assertThatThrownBy(() -> declareAttackers(player2, List.of(indexOf(player2, returned))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void returningALegalTargetIsMandatory() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castSpecialist();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void triggerDoesNotReturnTargetThatHasLeftTheGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castSpecialist();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Extraction Specialist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void specialistGainsLifeWhenItDealsCombatDamage() {
        castSpecialist();
        Permanent specialist = findPermanent(player1, "Extraction Specialist");
        specialist.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(indexOf(player1, specialist)));
        resolveCombat(player1);

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    private void castSpecialist() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ExtractionSpecialist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent castSpecialistAndReturn(GrizzlyBears bears) {
        harness.setGraveyard(player1, List.of(bears));
        castSpecialist();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        return findPermanentByCardId(player1, bears.getId());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private Permanent findPermanentByCardId(Player player, UUID cardId) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
