package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrownOfDoom.class, GrizzlyBears.class})
class CrownOfDoomTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's attacking creature gets +2/+0 until end of turn")
    void attackingCreatureGetsBoost() {
        addCrown(player1, player1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The controller's own attacking creature does not trigger Crown of Doom")
    void ownAttackingCreatureDoesNotTrigger() {
        addCrown(player1, player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The transfer ability returns Crown of Doom at the next cleanup")
    void transferReturnsAtCleanup() {
        Permanent crown = addCrown(player1, player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.findPermanentController(gd, crown.getId())).isEqualTo(player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gqs.findPermanentController(gd, crown.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The ability cannot target the owner, but can target a non-owner controller")
    void targetMustBeOtherThanOwner() {
        Permanent crown = addCrown(player2, player1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("owner");

        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentController(gd, crown.getId())).isEqualTo(player2.getId());
    }

    @Test
    void abilityCanActivateOnlyDuringItsControllerTurn() {
        addCrown(player1, player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private Permanent addCrown(Player controller, Player owner) {
        CrownOfDoom card = new CrownOfDoom();
        card.setOwnerId(owner.getId());
        return harness.addToBattlefieldAndReturn(controller, card);
    }
}
