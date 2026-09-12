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
}
