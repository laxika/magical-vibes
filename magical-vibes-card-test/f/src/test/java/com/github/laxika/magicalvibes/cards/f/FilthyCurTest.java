package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DiligentFarmhand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FilthyCur.class, Firebolt.class, DiligentFarmhand.class})
class FilthyCurTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage makes Filthy Cur's controller lose that much life")
    void nonCombatDamageMakesControllerLoseLife() {
        Permanent cur = harness.addToBattlefieldAndReturn(player2, new FilthyCur());
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, cur.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Filthy Cur");
    }

    @Test
    @DisplayName("Combat damage makes Filthy Cur's controller lose that much life")
    void combatDamageMakesControllerLoseLife() {
        addCreatureReady(player1, new DiligentFarmhand());
        addCreatureReady(player2, new FilthyCur());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player2, "Filthy Cur");
    }

    @Test
    @DisplayName("Both Curs' lethal combat damage triggers survive their sources dying")
    void lethalCombatDamageTriggersForBothControllers() {
        addCreatureReady(player1, new FilthyCur());
        addCreatureReady(player2, new FilthyCur());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Filthy Cur");
        harness.assertInGraveyard(player2, "Filthy Cur");
    }

    @Test
    @DisplayName("Damage to another creature does not trigger Filthy Cur")
    void damageToAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player2, new FilthyCur());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new DiligentFarmhand());
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, other.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Diligent Farmhand");
        harness.assertOnBattlefield(player2, "Filthy Cur");
    }
}
