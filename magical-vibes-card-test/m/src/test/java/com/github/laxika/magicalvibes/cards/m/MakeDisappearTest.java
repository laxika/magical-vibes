package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MakeDisappear.class, GrizzlyBears.class, CivilServant.class})
class MakeDisappearTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell when its controller cannot pay {2}")
    void countersTargetSpellWhenControllerCannotPay() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MakeDisappear()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not counter the target spell when its controller pays {2}")
    void doesNotCounterTargetSpellWhenControllerPays() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new MakeDisappear()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casualty copies Make Disappear and sacrifices the chosen creature")
    void casualtyCopiesSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        Permanent casualtyCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MakeDisappear()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, bears.getId(), casualtyCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The controller may decline an affordable payment")
    void countersWhenAffordablePaymentIsDeclined() {
        CivilServant target = new CivilServant();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new MakeDisappear()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Civil Servant");
        harness.assertNotOnBattlefield(player1, "Civil Servant");
        harness.assertInGraveyard(player2, "Make Disappear");
    }

    @Test
    @DisplayName("Paying for the casualty copy does not pay for the original")
    void originalCountersAfterControllerPaysOnlyForCopy() {
        castCasualtyWithPaymentAvailable(2);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Civil Servant");
        harness.assertNotOnBattlefield(player1, "Civil Servant");
        harness.assertInGraveyard(player2, "Make Disappear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying separately for both copies allows the target spell to resolve")
    void targetResolvesAfterBothPayments() {
        castCasualtyWithPaymentAvailable(4);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Civil Servant");
        harness.assertNotInGraveyard(player1, "Civil Servant");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Make Disappear"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The casualty copy may target and counter the original spell")
    void casualtyCopyMayChooseNewTarget() {
        CivilServant target = new CivilServant();
        MakeDisappear original = new MakeDisappear();
        Permanent sacrifice = addCreatureReady(player2, new CivilServant());
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(original));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, original.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Civil Servant");
        harness.assertInGraveyard(player2, "Make Disappear");
        harness.assertInGraveyard(player2, "Civil Servant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casualty rejects a creature whose effective power is zero")
    void casualtyRejectsZeroPowerCreature() {
        CivilServant target = new CivilServant();
        Permanent sacrifice = addCreatureReady(player2, new CivilServant());
        sacrifice.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new MakeDisappear()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player2, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Civil Servant");
        harness.assertNotInGraveyard(player2, "Civil Servant");
        harness.assertInHand(player2, "Make Disappear");
        assertThat(gd.stack).hasSize(1);
    }
    private void castCasualtyWithPaymentAvailable(int paymentMana) {
        CivilServant target = new CivilServant();
        Permanent sacrifice = addCreatureReady(player2, new CivilServant());
        sacrifice.tap();
        sacrifice.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, paymentMana);
        harness.setHand(player2, List.of(new MakeDisappear()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, target.getId(), sacrifice.getId());
        harness.assertNotOnBattlefield(player2, "Civil Servant");
        harness.assertInGraveyard(player2, "Civil Servant");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
    }
}
