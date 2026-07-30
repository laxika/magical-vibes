package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class DreadSlaverTest extends BaseCardTest {

    /** Dread Slaver attacks, Grizzly Bears blocks and dies to the combat damage. */
    private void slaverKillsBearsInCombat() {
        harness.addToBattlefield(player1, new DreadSlaver());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent slaver = gd.playerBattlefields.get(player1.getId()).getFirst();
        slaver.setSummoningSick(false);
        slaver.setAttacking(true);

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        bears.setSummoningSick(false);
        bears.setBlocking(true);
        bears.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A creature the Slaver kills returns under its controller immediately")
    void returnsDamagedCreatureUnderControl() {
        slaverKillsBearsInCombat();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The returned creature is a black Zombie in addition to its other colors and types")
    void returnedCreatureIsBlackZombie() {
        slaverKillsBearsInCombat();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, bears)).contains(CardColor.BLACK, CardColor.GREEN);
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.ZOMBIE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.BEAR)).isTrue();
    }

    @Test
    @DisplayName("A creature the Slaver never damaged is not returned when it dies")
    void noReturnForUndamagedCreature() {
        harness.addToBattlefield(player1, new DreadSlaver());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Non-combat damage from the Slaver also enables the return")
    void returnsCreatureDamagedOutsideCombat() {
        harness.addToBattlefield(player1, new DreadSlaver());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent slaver = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        gd.creatureCardsDamagedThisTurnBySourcePermanent
                .computeIfAbsent(slaver.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(bears.getCard().getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
