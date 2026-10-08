package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.Greed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImmolatingSouleater;
import com.github.laxika.magicalvibes.cards.m.MurderousBetrayal;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerrakWarpedSengir.class, Greed.class, GrizzlyBears.class, ProdigalPyromancer.class,
        ImmolatingSouleater.class, MurderousBetrayal.class})
class VerrakWarpedSengirTest extends BaseCardTest {

    @Test
    @DisplayName("May pay the life again to copy an ability with a life cost")
    void copiesLifePaidAbility() {
        harness.addToBattlefield(player1, new VerrakWarpedSengir());
        harness.addToBattlefield(player1, new Greed());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not trigger for an ability that did not require life")
    void doesNotTriggerWithoutLifePayment() {
        harness.addToBattlefield(player1, new VerrakWarpedSengir());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 1, null, player2.getId());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Declining the second life payment leaves only the original ability")
    void mayDeclineLifePayment() {
        harness.addToBattlefield(player1, new VerrakWarpedSengir());
        harness.addToBattlefield(player1, new Greed());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An opponent paying life does not trigger Verrak")
    void opponentLifePaymentDoesNotTrigger() {
        harness.addToBattlefield(player1, new VerrakWarpedSengir());
        harness.addToBattlefield(player2, new Greed());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Life paid for Phyrexian mana can be paid again to copy the ability")
    void copiesAbilityPaidWithPhyrexianLife() {
        harness.addToBattlefield(player1, new VerrakWarpedSengir());
        Permanent souleater = harness.addToBattlefieldAndReturn(player1, new ImmolatingSouleater());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.assertLife(player1, 18);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 16);
        assertThat(souleater.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying Phyrexian mana with mana does not trigger Verrak")
    void phyrexianManaPaidWithManaDoesNotTrigger() {
        harness.addToBattlefield(player1, new VerrakWarpedSengir());
        Permanent souleater = harness.addToBattlefieldAndReturn(player1, new ImmolatingSouleater());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(souleater.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Half-life costs are copied at the amount actually paid and copies may choose new targets")
    void repeatsActualHalfLifePaymentAndRetargetsCopy() {
        harness.addToBattlefield(player1, new VerrakWarpedSengir());
        harness.addToBattlefield(player1, new MurderousBetrayal());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, originalTarget.getId());
        harness.assertLife(player1, 10);
        harness.setLife(player1, 30);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Insufficient life prevents the second payment and the copy")
    void cannotCopyWithoutEnoughLife() {
        harness.addToBattlefield(player1, new VerrakWarpedSengir());
        harness.addToBattlefield(player1, new Greed());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 1);
    }
}
