package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CanopyGorger;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.cards.o.OverwhelmingDenial;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EldraziObligator.class, CanopyGorger.class, Wastes.class, OverwhelmingDenial.class})
class EldraziObligatorTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{C} steals, untaps and grants haste to the target creature")
    void payingManaStealsUntapsAndGrantsHaste() {
        Permanent target = addTargetCreature();
        target.tap();
        castEldraziObligator(target.getId());

        resolveCastTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Declining the payment leaves the target unchanged")
    void decliningPaymentDoesNothing() {
        Permanent target = addTargetCreature();
        target.tap();
        castEldraziObligator(target.getId());

        resolveCastTrigger();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addTargetCreature();
        castEldraziObligator(target.getId());

        resolveCastTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The cast trigger cannot target a land")
    void rejectsLandTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Wastes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new EldraziObligator(), "{2}{R}");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Colored mana cannot pay the colorless part of the trigger cost")
    void coloredManaCannotPayColorlessCost() {
        Permanent target = addTargetCreature();
        target.tap();
        castEldraziObligator(target.getId());
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.RED, 2);

        resolveCastTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        harness.assertOnBattlefield(player2, "Canopy Gorger");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger can untap a creature already controlled by its controller")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        target.tap();
        castEldraziObligator(target.getId());

        resolveCastTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Canopy Gorger");
    }

    @Test
    @DisplayName("The cast trigger resolves before the creature spell")
    void triggerResolvesBeforeCreatureSpell() {
        Permanent target = addTargetCreature();
        castEldraziObligator(target.getId());

        resolveCastTrigger();
        harness.assertNotOnBattlefield(player1, "Eldrazi Obligator");
        harness.assertOnBattlefield(player2, "Canopy Gorger");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Canopy Gorger");
        harness.assertNotOnBattlefield(player1, "Eldrazi Obligator");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Eldrazi Obligator");
    }

    @Test
    @DisplayName("A target leaving the battlefield prevents resolution and payment")
    void lostTargetPreventsPayment() {
        Permanent target = addTargetCreature();
        castEldraziObligator(target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Eldrazi Obligator");
        harness.assertNotOnBattlefield(player1, "Canopy Gorger");
    }

    @Test
    @DisplayName("Entering without being cast does not trigger the theft ability")
    void enteringWithoutCastingDoesNotTrigger() {
        Permanent target = addTargetCreature();
        target.tap();
        harness.enterBattlefieldAndReturn(player1, new EldraziObligator());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(target.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Canopy Gorger");
    }

    @Test
    @DisplayName("Countering the creature spell does not remove its cast trigger")
    void triggerSurvivesCounteringCreatureSpell() {
        Permanent target = addTargetCreature();
        target.tap();
        castEldraziObligator(target.getId());
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.setHand(player2, List.of(new OverwhelmingDenial()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, spellId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Eldrazi Obligator");

        resolveCastTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Canopy Gorger");
        harness.assertNotOnBattlefield(player1, "Eldrazi Obligator");
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The creature spell can resolve when there are no creatures to target")
    void canCastWithoutAvailableTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new EldraziObligator(), "{2}{R}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eldrazi Obligator");
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private Permanent addTargetCreature() {
        return harness.addToBattlefieldAndReturn(player2, new CanopyGorger());
    }

    private void castEldraziObligator(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new EldraziObligator(), "{2}{R}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handlePermanentChosen(player1, targetId);
    }

    private void resolveCastTrigger() {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
