package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.v.VentSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({HazeFrog.class, GlorySeeker.class, VentSentinel.class})
class HazeFrogTest extends BaseCardTest {

    @Test
    @DisplayName("Its ETB prevents other creatures' combat damage but not Haze Frog's")
    void preventsOtherCreaturesCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GlorySeeker());

        harness.castFromHand(player1, new HazeFrog(), "{3}{G}{G}");
        resolveAllTriggers();

        Permanent frog = findPermanent(player1, "Haze Frog");
        frog.setSummoningSick(false);
        declareAttackers(List.of(0, 1));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Flash allows Haze Frog to enter on the opponent's turn and kill an attacker safely")
    void flashAndBlockingPreventDamageFromOpponent() {
        addCreatureReady(player2, new GlorySeeker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new HazeFrog(), "{3}{G}{G}");
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Haze Frog");
        harness.assertInGraveyard(player2, "Glory Seeker");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Two resolved Haze Frog triggers each prevent the other Frog's damage")
    void overlappingTriggersPreventBothFrogsDamage() {
        harness.castFromHand(player1, new HazeFrog(), "{3}{G}{G}");
        resolveAllTriggers();
        harness.castFromHand(player1, new HazeFrog(), "{3}{G}{G}");
        resolveAllTriggers();
        findPermanents(player1, "Haze Frog").forEach(frog -> frog.setSummoningSick(false));

        declareAttackers(List.of(0, 1));
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The prevention also applies to creatures entering after the trigger resolves")
    void preventsCombatDamageFromLaterArrivals() {
        harness.castFromHand(player1, new HazeFrog(), "{3}{G}{G}");
        resolveAllTriggers();
        addCreatureReady(player1, new GlorySeeker());

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Combat damage prevention expires when the turn ends")
    void preventionExpiresAtEndOfTurn() {
        harness.castFromHand(player1, new HazeFrog(), "{3}{G}{G}");
        resolveAllTriggers();
        addCreatureReady(player2, new GlorySeeker());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Noncombat damage from other creatures is not prevented")
    void allowsNoncombatDamageFromOtherCreatures() {
        addCreatureReady(player1, new VentSentinel());
        harness.castFromHand(player1, new HazeFrog(), "{3}{G}{G}");
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }
}
