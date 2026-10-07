package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.RoostOfDrakes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteampathCharger.class, GrizzlyBears.class, RoostOfDrakes.class})
class SteampathChargerTest extends BaseCardTest {

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new SteampathCharger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    void doesNotCreateOffspringTokenWhenNotPaid() {
        harness.setHand(player1, List.of(new SteampathCharger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void deathTriggerDealsOneDamageToTargetPlayer() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new SteampathCharger());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        charger.setSummoningSick(false);
        charger.setAttacking(true);

        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void offspringTokenRetainsDeathTriggerAndCanTargetItsController() {
        harness.setHand(player1, List.of(new SteampathCharger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        token.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void offspringStillCreatesTokenAfterOriginalDiesBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new SteampathCharger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Steampath Charger");
        charger.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Steampath Charger");
        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastDoesNotCreateOffspring() {
        harness.enterBattlefieldAndReturn(player1, new SteampathCharger());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingOffspringDoesNotTriggerAbilitiesForKickedSpells() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.setHand(player1, List.of(new SteampathCharger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                });
    }
}
