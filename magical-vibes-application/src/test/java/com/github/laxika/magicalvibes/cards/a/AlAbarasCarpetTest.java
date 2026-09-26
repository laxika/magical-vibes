package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FrostGiant;
import com.github.laxika.magicalvibes.cards.p.Pyrotechnics;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlAbarasCarpet.class, FrostGiant.class, AzureDrake.class, Pyrotechnics.class})
class AlAbarasCarpetTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from non-flying attackers but not flying attackers")
    void preventsDamageFromNonFlyingAttackersOnly() {
        Permanent carpet = harness.addToBattlefieldAndReturn(player1, new AlAbarasCarpet());
        addCreatureReady(player2, new FrostGiant());
        addCreatureReady(player2, new AzureDrake());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(carpet), null, null);
        assertThat(carpet.isTapped()).isTrue();
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prevention expires at end of turn")
    void preventionExpiresAtEndOfTurn() {
        Permanent carpet = harness.addToBattlefieldAndReturn(player1, new AlAbarasCarpet());
        addCreatureReady(player2, new FrostGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(carpet), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Does not prevent damage from noncreature sources")
    void doesNotPreventDamageFromNoncreatureSources() {
        Permanent carpet = harness.addToBattlefieldAndReturn(player1, new AlAbarasCarpet());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(carpet), null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Pyrotechnics()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castSorcery(player2, 0, Map.of(player1.getId(), 4));
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }
}
