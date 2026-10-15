package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CurseOfThePiercedHeart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.r.Replenish;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({AbsoluteVirtue.class, Cancel.class, Shock.class, GrizzlyBears.class,
        Hurricane.class, Unsummon.class, CurseOfThePiercedHeart.class, Replenish.class})
class AbsoluteVirtueTest extends BaseCardTest {

    @Test
    @DisplayName("Absolute Virtue can't be countered")
    void cannotBeCountered() {
        AbsoluteVirtue virtue = new AbsoluteVirtue();
        harness.setHand(player1, List.of(virtue));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, virtue.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Absolute Virtue");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Protection from opponents protects only the controller")
    void protectionIsPlayerScoped() {
        Permanent virtue = harness.addToBattlefieldAndReturn(player1, new AbsoluteVirtue());

        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");

        harness.castAndResolveInstant(player2, 0, virtue.getId());
        assertThat(virtue.getMarkedDamage()).isEqualTo(2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Protection from opponents prevents combat damage to the controller")
    void preventsOpponentCombatDamage() {
        harness.addToBattlefield(player1, new AbsoluteVirtue());
        harness.setLife(player1, 20);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Protection prevents untargeted opponent damage but does not protect the creature")
    void preventsUntargetedOpponentDamage() {
        Permanent virtue = harness.addToBattlefieldAndReturn(player1, new AbsoluteVirtue());
        harness.setHand(player2, List.of(new Hurricane()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, 2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(virtue.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Protection does not prevent damage from the controller's own spell")
    void allowsOwnUntargetedDamage() {
        harness.addToBattlefield(player1, new AbsoluteVirtue());
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Gaining protection makes an opponent's pending player target illegal")
    void invalidatesPendingOpponentTarget() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, player1.getId());

        harness.addToBattlefield(player1, new AbsoluteVirtue());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Protection ends when Absolute Virtue leaves the battlefield")
    void protectionEndsWhenVirtueLeaves() {
        Permanent virtue = harness.addToBattlefieldAndReturn(player1, new AbsoluteVirtue());
        harness.setHand(player2, List.of(new Unsummon(), new Shock()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player2, 0, virtue.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertInHand(player1, "Absolute Virtue");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Gaining protection removes an opponent's Aura enchanting the controller")
    void removesOpponentPlayerAura() {
        harness.setHand(player2, List.of(new CurseOfThePiercedHeart()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Curse of the Pierced Heart");

        harness.addToBattlefield(player1, new AbsoluteVirtue());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Curse of the Pierced Heart");
        harness.assertInGraveyard(player2, "Curse of the Pierced Heart");
    }

    @Test
    @DisplayName("An opponent's returning Aura can enchant only the unprotected player")
    void returningAuraCannotEnchantProtectedOpponent() {
        harness.addToBattlefield(player1, new AbsoluteVirtue());
        harness.setGraveyard(player2, List.of(new CurseOfThePiercedHeart()));
        harness.setHand(player2, List.of(new Replenish()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Curse of the Pierced Heart");
        assertThat(findPermanent(player2, "Curse of the Pierced Heart").getAttachedTo())
                .isEqualTo(player2.getId());
    }
}
