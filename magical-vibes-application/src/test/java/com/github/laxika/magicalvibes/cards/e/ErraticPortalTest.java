package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CrashingBoars;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErraticPortal.class, CrashingBoars.class})
class ErraticPortalTest extends BaseCardTest {

    @Test
    @DisplayName("The target creature's controller is offered the payment")
    void targetControllerIsOfferedPayment() {
        addPortal();
        Permanent target = addCreatureReady(player2, new CrashingBoars());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        activate(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Paying {1} keeps the target creature on the battlefield")
    void payingKeepsTargetCreature() {
        addPortal();
        Permanent target = addCreatureReady(player2, new CrashingBoars());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        activate(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Crashing Boars");
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the payment returns the target creature to its owner's hand")
    void decliningReturnsTargetCreature() {
        addPortal();
        Permanent target = addCreatureReady(player2, new CrashingBoars());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(target);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Crashing Boars");
        harness.assertInHand(player2, "Crashing Boars");
    }

    @Test
    @DisplayName("A target controller without {1} cannot keep the creature")
    void targetControllerWithoutManaCannotPayToKeepCreature() {
        addPortal();
        Permanent target = addCreatureReady(player2, new CrashingBoars());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(target);

        harness.handleMayAbilityChosen(player2, true);
        harness.assertNotOnBattlefield(player2, "Crashing Boars");
        harness.assertInHand(player2, "Crashing Boars");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addPortal();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ErraticPortal());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> activate(target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        addPortal();
        Permanent target = addCreatureReady(player2, new CrashingBoars());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void activationPaysManaAndTapsPortal() {
        Permanent portal = harness.addToBattlefieldAndReturn(player1, new ErraticPortal());
        Permanent target = addCreatureReady(player2, new CrashingBoars());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(portal.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.assertInHand(player2, "Crashing Boars");
    }

    @Test
    void cannotActivateWithoutMana() {
        addPortal();
        Permanent target = addCreatureReady(player2, new CrashingBoars());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Erratic Portal").isTapped()).isFalse();
    }

    @Test
    void cannotActivateTappedPortal() {
        addPortal();
        findPermanent(player1, "Erratic Portal").tap();
        Permanent target = addCreatureReady(player2, new CrashingBoars());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canReturnOwnCreatureEvenWhenAbleToPay() {
        addPortal();
        Permanent target = addCreatureReady(player1, new CrashingBoars());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activate(target);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Crashing Boars");
        harness.assertInHand(player1, "Crashing Boars");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void stolenCreatureReturnsToOwnerButControllerChoosesPayment() {
        addPortal();
        Permanent target = addCreatureReady(player2, new CrashingBoars());
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        activate(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.assertNotOnBattlefield(player2, "Crashing Boars");
        harness.assertInHand(player1, "Crashing Boars");
        harness.assertNotInHand(player2, "Crashing Boars");
    }

    @Test
    void payingWithColoredManaSpendsOnlyControllersMana() {
        addPortal();
        Permanent target = addCreatureReady(player2, new CrashingBoars());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);

        activate(target);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Crashing Boars");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private void addPortal() {
        harness.addToBattlefield(player1, new ErraticPortal());
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
