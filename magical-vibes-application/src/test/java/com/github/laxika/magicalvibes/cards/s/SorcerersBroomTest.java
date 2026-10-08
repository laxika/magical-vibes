package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrossHopper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WitchsOven;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SorcerersBroom.class, DrossHopper.class, GrizzlyBears.class, WitchsOven.class})
class SorcerersBroomTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another permanent offers to create a token copy")
    void sacrificingAnotherPermanentCreatesTokenCopyAfterPaying() {
        Permanent broom = addCreatureReady(player1, new SorcerersBroom());
        addCreatureReady(player1, new DrossHopper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Sorcerer's Broom")).hasSize(2);
        Permanent token = findPermanents(player1, "Sorcerer's Broom").stream()
                .filter(permanent -> !permanent.getId().equals(broom.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the payment does not create a token copy")
    void decliningPaymentDoesNothing() {
        addCreatureReady(player1, new SorcerersBroom());
        addCreatureReady(player1, new DrossHopper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Sorcerer's Broom")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("A sacrifice by an opponent does not trigger Sorcerer's Broom")
    void opponentSacrificeDoesNotTrigger() {
        harness.addToBattlefield(player1, new SorcerersBroom());
        addCreatureReady(player2, new DrossHopper());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, bears.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing the Broom itself does not trigger its ability")
    void sacrificingItselfDoesNotTrigger() {
        Permanent broom = addCreatureReady(player1, new SorcerersBroom());
        harness.addToBattlefield(player1, new WitchsOven());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, broom.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Sorcerer's Broom")).isEmpty();
        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("The copy is created even if the Broom dies before resolution")
    void sourceLeavingDoesNotPreventCopyAndCountersAreNotCopied() {
        Permanent broom = addCreatureReady(player1, new SorcerersBroom());
        broom.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player1, new DrossHopper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        broom.setMarkedDamage(3);
        harness.runStateBasedActions();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Sorcerer's Broom")).hasSize(1);
        Permanent token = findPermanent(player1, "Sorcerer's Broom");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getId()).isNotEqualTo(broom.getId());
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A token copy can make another copy of itself")
    void tokenCopyRetainsTriggeredAbility() {
        Permanent broom = addCreatureReady(player1, new SorcerersBroom());
        Permanent hopper = addCreatureReady(player1, new DrossHopper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        broom.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, hopper.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Sorcerer's Broom")).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
