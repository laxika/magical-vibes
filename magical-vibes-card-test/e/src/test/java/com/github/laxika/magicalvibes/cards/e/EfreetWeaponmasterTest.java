package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BringLow;
import com.github.laxika.magicalvibes.cards.s.SmokeTeller;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EfreetWeaponmaster.class, SmokeTeller.class, BringLow.class})
class EfreetWeaponmasterTest extends BaseCardTest {

    @Test
    void entersAndBoostsAnotherCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SmokeTeller());
        harness.setHand(player1, List.of(new EfreetWeaponmaster()));
        addNormalManaCost();

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void turningFaceUpPromptsForAnotherCreatureYouControlAndBoostsIt() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SmokeTeller());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SmokeTeller());
        harness.setHand(player1, List.of(new EfreetWeaponmaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent efreet = findPermanent(player1, "Efreet Weaponmaster");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(efreet));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(efreet.getId(), opponentCreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(efreet.isFaceDown()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void faceDownEntryDoesNotBoostAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SmokeTeller());
        harness.setHand(player1, List.of(new EfreetWeaponmaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Efreet Weaponmaster").isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SmokeTeller());
        harness.setHand(player1, List.of(new EfreetWeaponmaster()));
        addNormalManaCost();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(target.getEffectivePower()).isEqualTo(5);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void canEnterWithoutAnotherControlledCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SmokeTeller());
        harness.setHand(player1, List.of(new EfreetWeaponmaster()));
        addNormalManaCost();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Efreet Weaponmaster");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Efreet Weaponmaster").getPowerModifier()).isZero();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void canTurnFaceUpWithoutAnotherControlledCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SmokeTeller());
        harness.setHand(player1, List.of(new EfreetWeaponmaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent efreet = findPermanent(player1, "Efreet Weaponmaster");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(efreet));

        assertThat(efreet.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(efreet.getPowerModifier()).isZero();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void triggerStillBoostsTargetAfterWeaponmasterDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SmokeTeller());
        harness.setHand(player1, List.of(new EfreetWeaponmaster()));
        harness.setHand(player2, List.of(new BringLow()));
        addNormalManaCost();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent efreet = findPermanent(player1, "Efreet Weaponmaster");
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, efreet.getId());
        harness.assertInGraveyard(player1, "Efreet Weaponmaster");
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void triggerDoesNotBoostAnotherCreatureWhenItsTargetDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SmokeTeller());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new SmokeTeller());
        harness.setHand(player1, List.of(new EfreetWeaponmaster()));
        harness.setHand(player2, List.of(new BringLow()));
        addNormalManaCost();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player1, "Smoke Teller");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(otherCreature.getEffectivePower()).isEqualTo(2);
        assertThat(otherCreature.getEffectiveToughness()).isEqualTo(2);
    }

    private void addNormalManaCost() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
