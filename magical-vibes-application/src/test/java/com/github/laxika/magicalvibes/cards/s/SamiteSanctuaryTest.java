package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.r.RhysticLightning;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamiteSanctuary.class, AvatarOfMight.class, RhysticLightning.class})
class SamiteSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Any player may pay {2} to shield a target creature from the next damage")
    void anyPlayerMayActivateAndPreventNextDamage() {
        harness.addToBattlefield(player1, new SamiteSanctuary());
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);

        harness.setHand(player1, List.of(new RhysticLightning()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new SamiteSanctuary());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new SamiteSanctuary());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The shield applies only to the chosen creature")
    void shieldAppliesOnlyToChosenCreature() {
        harness.addToBattlefield(player1, new SamiteSanctuary());
        Permanent protectedTarget = addCreatureReady(player2, new AvatarOfMight());
        Permanent otherTarget = addCreatureReady(player2, new AvatarOfMight());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, protectedTarget.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new RhysticLightning()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, otherTarget.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(protectedTarget.getDamagePreventionShield()).isEqualTo(1);
        assertThat(protectedTarget.getMarkedDamage()).isZero();
        assertThat(otherTarget.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("An unused prevention shield expires at the end of the turn")
    void unusedShieldExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new SamiteSanctuary());
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Repeated activations stack and only prevent the next damage")
    void repeatedActivationsStackAndAreConsumed() {
        harness.addToBattlefield(player1, new SamiteSanctuary());
        Permanent target = addCreatureReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, target.getId());
        harness.activateAbility(player2, 0, null, target.getId());

        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);

        resolveAllTriggers();
        assertThat(target.getDamagePreventionShield()).isEqualTo(2);

        harness.setHand(player2, List.of(new RhysticLightning(), new RhysticLightning()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(target.getDamagePreventionShield()).isZero();

        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertOnBattlefield(player1, "Avatar of Might");
    }

    @Test
    @DisplayName("The activating player must pay the full cost from their own mana")
    void cannotUseControllersManaToActivate() {
        harness.addToBattlefield(player1, new SamiteSanctuary());
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new SamiteSanctuary());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }
}
