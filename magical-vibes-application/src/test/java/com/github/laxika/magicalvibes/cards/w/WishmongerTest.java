package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Wishmonger.class, FreshVolunteers.class, Plains.class})
class WishmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Any player may pay {2} to grant a target creature protection chosen by its controller")
    void anyPlayerMayActivateAndTargetControllerChooses() {
        harness.addToBattlefieldAndReturn(player1, new Wishmonger());
        Permanent target = addCreatureReady(player1, new FreshVolunteers());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Protection granted by Wishmonger wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new Wishmonger());
        Permanent target = addCreatureReady(player1, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("The target's controller chooses the color for an opponent-controlled target")
    void targetControllerChoosesForOpponentControlledCreature() {
        harness.addToBattlefieldAndReturn(player1, new Wishmonger());
        Permanent target = addCreatureReady(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player2, "GREEN");

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefieldAndReturn(player1, new Wishmonger());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Wishmonger can activate without tapping")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent wishmonger = harness.addToBattlefieldAndReturn(player1, new Wishmonger());
        wishmonger.setSummoningSick(true);
        wishmonger.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, wishmonger.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThat(gqs.hasProtectionFrom(gd, wishmonger, CardColor.WHITE)).isTrue();
        assertThat(wishmonger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Protection from white prevents further targeting by Wishmonger")
    void protectionFromWhitePreventsFurtherTargeting() {
        harness.addToBattlefieldAndReturn(player1, new Wishmonger());
        Permanent target = addCreatureReady(player1, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An activation does not resolve if its target gains protection from white in response")
    void protectionFromWhiteMakesPendingActivationIllegal() {
        harness.addToBattlefieldAndReturn(player1, new Wishmonger());
        Permanent target = addCreatureReady(player1, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.WHITE)).isTrue();
    }
}
