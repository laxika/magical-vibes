package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.e.ExquisiteFirecraft;
import com.github.laxika.magicalvibes.cards.f.FlowstoneKavu;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JayaFieryNegotiator;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RithLiberatedPrimeval.class, ExquisiteFirecraft.class, HillGiant.class,
        ShivanDragon.class, Shock.class, DragonWhelp.class, FlowstoneKavu.class,
        JayaFieryNegotiator.class, LightningStrike.class})
class RithLiberatedPrimevalTest extends BaseCardTest {

    @Test
    void createsDragonAtYourEndStepAfterExcessDamage() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castFirecraft(target);

        advanceToEndStep();

        assertThat(findPermanents(player1, "Dragon").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    void doesNotCreateDragonWithoutExcessDamage() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        advanceToEndStep();

        assertThat(findPermanents(player1, "Dragon").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    @Test
    void otherDragonsHaveWardTwo() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, dragon.getId());

        harness.assertInGraveyard(player2, "Shock");
        assertThat(dragon.getMarkedDamage()).isZero();
    }

    @Test
    void exactlyLethalDamageDoesNotQualify() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlowstoneKavu());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player2, "Flowstone Kavu");
        advanceToEndStep();

        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    void damageAlreadyMarkedCountsTowardExcessDamage() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlowstoneKavu());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player2, "Flowstone Kavu");
        advanceToEndStep();

        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
    }

    @Test
    void excessDamageToAPlaneswalkerQualifies() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.enterBattlefieldAndReturn(player2, new JayaFieryNegotiator());
        harness.setHand(player1, List.of(new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertOnBattlefield(player2, "Jaya, Fiery Negotiator");
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player2, "Jaya, Fiery Negotiator");
        advanceToEndStep();

        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
    }

    @Test
    void exactlyLethalPlaneswalkerDamageDoesNotQualify() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.enterBattlefieldAndReturn(player2, new JayaFieryNegotiator());
        castFirecraft(target);

        harness.assertInGraveyard(player2, "Jaya, Fiery Negotiator");
        advanceToEndStep();

        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    void excessDamageToYourOwnCreatureDoesNotQualify() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FlowstoneKavu());
        castFirecraft(target);

        advanceToEndStep();

        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    void excessDamageBeforeRithEntersStillQualifies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlowstoneKavu());
        castFirecraft(target);
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());

        advanceToEndStep();

        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
    }

    @Test
    void multipleExcessDamageEventsCreateOnlyOneDragon() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FlowstoneKavu());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FlowstoneKavu());
        castFirecraft(first);
        castFirecraft(second);

        advanceToEndStep();

        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
    }

    @Test
    void doesNotTriggerAtAnOpponentsEndStep() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlowstoneKavu());
        castFirecraft(target);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    void rithCountersAnOpponentsSpellWhenWardCannotBePaid() {
        Permanent rith = harness.addToBattlefieldAndReturn(player1, new RithLiberatedPrimeval());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, rith.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Lightning Strike");
        assertThat(rith.getMarkedDamage()).isZero();
    }

    @Test
    void rithHasOnlyItsOwnWardTwo() {
        Permanent rith = harness.addToBattlefieldAndReturn(player1, new RithLiberatedPrimeval());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castAndResolveInstant(player2, 0, rith.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(rith.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void payingGrantedWardAllowsDamageToAnotherDragon() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castAndResolveInstant(player2, 0, dragon.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dragon Whelp");
    }

    @Test
    void nonDragonsDoNotReceiveWard() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FlowstoneKavu());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Flowstone Kavu");
    }

    @Test
    void opponentsDragonsDoNotReceiveWard() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DragonWhelp());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Dragon Whelp");
    }

    @Test
    void controllerCanTargetItsOwnDragonWithoutPayingWard() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dragon Whelp");
    }

    private void castFirecraft(Permanent target) {
        harness.setHand(player1, List.of(new ExquisiteFirecraft()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
