package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CuriousPair;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.ShiftingCeratops;
import com.github.laxika.magicalvibes.cards.t.TomeRaider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticalDispute.class, TomeRaider.class, CuriousPair.class, MaraleafPixie.class,
        Forest.class})
class MysticalDisputeTest extends BaseCardTest {

    @Test
    void costsOneBlueWhenTargetingBlueSpell() {
        TomeRaider raider = new TomeRaider();
        harness.castFromHand(player1, raider, "{2}{U}");
        harness.setHand(player2, List.of(new MysticalDispute()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, raider.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void costsFullAmountWhenTargetingNonBlueSpell() {
        CuriousPair pair = new CuriousPair();
        harness.castFromHand(player1, pair, "{1}{G}");
        harness.setHand(player2, List.of(new MysticalDispute()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, pair.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void countersTargetSpellWhenItsControllerCannotPay() {
        CuriousPair pair = new CuriousPair();
        harness.castFromHand(player1, pair, "{1}{G}");
        harness.setHand(player2, List.of(new MysticalDispute()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, pair.getId());

        harness.assertInGraveyard(player1, "Curious Pair");
        harness.assertInGraveyard(player2, "Mystical Dispute");
        harness.assertNotOnBattlefield(player1, "Curious Pair");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCounterTargetSpellWhenItsControllerPays() {
        CuriousPair pair = new CuriousPair();
        harness.castFromHand(player1, pair, "{1}{G}");
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new MysticalDispute()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, pair.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Curious Pair");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void controllerCanDeclinePaymentEvenWithEnoughMana() {
        CuriousPair pair = new CuriousPair();
        harness.castFromHand(player1, pair, "{1}{G}");
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new MysticalDispute()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, pair.getId());

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Curious Pair");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void costsOneBlueWhenTargetingMulticoloredBlueSpell() {
        MaraleafPixie pixie = new MaraleafPixie();
        harness.castFromHand(player1, pixie, "{G}{U}");
        harness.setHand(player2, List.of(new MysticalDispute()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, pixie.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Maraleaf Pixie");
    }

    @Test
    void cannotUseBlueSpellDiscountForNonBlueTarget() {
        CuriousPair pair = new CuriousPair();
        harness.castFromHand(player1, pair, "{1}{G}");
        MysticalDispute dispute = new MysticalDispute();
        harness.setHand(player2, List.of(dispute));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, pair.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(dispute);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canTargetOwnSpellAndPayWithMixedMana() {
        CuriousPair pair = new CuriousPair();
        harness.castFromHand(player1, pair, "{1}{G}");
        harness.setHand(player1, List.of(new MysticalDispute()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, pair.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Curious Pair");
        harness.assertInGraveyard(player1, "Mystical Dispute");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void controllerCanGeneratePaymentManaDuringResolution() {
        CuriousPair pair = new CuriousPair();
        harness.castFromHand(player1, pair, "{1}{G}");
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player2, List.of(new MysticalDispute()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, pair.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        for (int i = 0; i < 3; i++) {
            gs.tapPermanent(gd, player1, i);
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Curious Pair");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @CardUsed(ShiftingCeratops.class)
    void controllerMayPayEvenWhenTargetCannotBeCountered() {
        ShiftingCeratops ceratops = new ShiftingCeratops();
        harness.castFromHand(player1, ceratops, "{2}{G}{G}");
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new MysticalDispute()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ceratops.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shifting Ceratops");
    }
}
