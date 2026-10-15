package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarduBlazebringer.class, WetlandSambar.class})
class MarduBlazebringerTest extends BaseCardTest {

    @Test
    @DisplayName("Mardu Blazebringer is sacrificed at end of combat after attacking")
    void sacrificedAtEndOfCombatWhenAttacking() {
        addCreatureReady(player1, new MarduBlazebringer());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player1, "Mardu Blazebringer");
        harness.assertInGraveyard(player1, "Mardu Blazebringer");
    }

    @Test
    @DisplayName("Mardu Blazebringer is sacrificed at end of combat after blocking")
    void sacrificedAtEndOfCombatWhenBlocking() {
        addCreatureReady(player2, new MarduBlazebringer());

        Permanent attacker = addCreatureReady(player1, new WetlandSambar());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player2, "Mardu Blazebringer");
        harness.assertInGraveyard(player2, "Mardu Blazebringer");
    }

    @Test
    @DisplayName("End-of-combat sacrifice uses the stack and allows a response")
    void sacrificeWaitsForDelayedTriggerToResolve() {
        addCreatureReady(player1, new MarduBlazebringer());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Mardu Blazebringer");
        harness.assertNotInGraveyard(player1, "Mardu Blazebringer");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mardu Blazebringer");
        harness.assertInGraveyard(player1, "Mardu Blazebringer");
    }

    @Test
    @DisplayName("A creature that neither attacks nor blocks survives combat")
    void survivesWithoutAttackingOrBlocking() {
        addCreatureReady(player1, new MarduBlazebringer());
        declareAttackers(List.of());

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player1, "Mardu Blazebringer");
        harness.assertNotInGraveyard(player1, "Mardu Blazebringer");
    }

    @Test
    @DisplayName("The delayed ability cannot sacrifice a creature now controlled by an opponent")
    void cannotSacrificeAfterLosingControl() {
        Permanent blazebringer = addCreatureReady(player1, new MarduBlazebringer());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        gd.playerBattlefields.get(player1.getId()).remove(blazebringer);
        gd.playerBattlefields.get(player2.getId()).add(blazebringer);
        blazebringer.setAttacking(false);
        blazebringer.setSummoningSick(true);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player2, "Mardu Blazebringer");
        harness.assertNotInGraveyard(player1, "Mardu Blazebringer");
        harness.assertNotInGraveyard(player2, "Mardu Blazebringer");
    }
}
