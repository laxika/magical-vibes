package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HandToHandTest extends BaseCardTest {

    @Test
    @DisplayName("Controller can't cast an instant during combat")
    void controllerCantCastInstantDuringCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent can't cast an instant during combat either")
    void opponentCantCastInstantDuringCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Instants can still be cast outside combat")
    void instantCastableOutsideCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Non-mana activated abilities can't be activated during combat")
    void nonManaAbilityBlockedDuringCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player2, new AdantoVanguard());
        vanguard.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during combat");
    }

    @Test
    @DisplayName("Mana abilities still work during combat")
    void manaAbilityAllowedDuringCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.tapPermanent(player2, 0);

        assertThat(elves.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-mana activated abilities work outside combat")
    void nonManaAbilityAllowedOutsideCombat() {
        harness.addToBattlefield(player1, new HandToHand());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player2, new AdantoVanguard());
        vanguard.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
