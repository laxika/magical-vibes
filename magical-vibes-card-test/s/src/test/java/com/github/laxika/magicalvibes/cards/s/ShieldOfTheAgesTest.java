package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldOfTheAges.class, Incinerate.class, BalduvianBears.class, Shatter.class})
class ShieldOfTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 damage from a larger hit dealt to its controller")
    void preventsOneOfLargerHit() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ShieldOfTheAges());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        // 3 damage: 1 prevented, 2 through.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Activating twice prevents 2 of 3 damage")
    void activatingTwiceStacksShield() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ShieldOfTheAges());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to its controller")
    void preventsCombatDamageToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ShieldOfTheAges());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        addCreatureReady(player2, new BalduvianBears());
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not prevent damage dealt to an opponent")
    void doesNotPreventDamageDealtToOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ShieldOfTheAges());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Shield wears off at end of turn")
    void shieldWearsOffAtEndOfTurn() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ShieldOfTheAges());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A shield is consumed by the first damage event")
    void shieldIsConsumedOnlyOnce() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ShieldOfTheAges());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Incinerate(), new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Removing the artifact does not remove a resolved shield")
    void shieldSurvivesArtifactDestruction() {
        harness.setLife(player1, 20);
        var shield = harness.addToBattlefieldAndReturn(player1, new ShieldOfTheAges());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shatter(), new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, shield.getId());
        harness.assertInGraveyard(player1, "Shield of the Ages");
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Damage to a controlled creature does not use the player's shield")
    void creatureDamageDoesNotConsumeShield() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ShieldOfTheAges());
        var bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Incinerate(), new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.assertInGraveyard(player1, "Balduvian Bears");
        harness.assertLife(player1, 20);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
    }
}
