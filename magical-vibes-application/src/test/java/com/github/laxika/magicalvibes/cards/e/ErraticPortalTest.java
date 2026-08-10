package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ErraticPortalTest extends BaseCardTest {

    @Test
    @DisplayName("The target creature's controller is offered the payment")
    void targetControllerIsOfferedPayment() {
        addPortal();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(target);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Paying {1} keeps the target creature on the battlefield")
    void payingKeepsTargetCreature() {
        addPortal();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        activate(target);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the payment returns the target creature to its owner's hand")
    void decliningReturnsTargetCreature() {
        addPortal();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(target);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addPortal();
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> activate(island))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addPortal() {
        harness.addToBattlefield(player1, new ErraticPortal());
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
