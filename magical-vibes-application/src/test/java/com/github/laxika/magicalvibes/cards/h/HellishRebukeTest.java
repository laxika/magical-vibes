package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
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

@CardUsed({HellishRebuke.class, GrizzlyBears.class, ProdigalSorcerer.class})
class HellishRebukeTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent permanent that damages the caster is sacrificed and its controller loses 2 life")
    void punishesCombatDamageToCaster() {
        harness.setHand(player1, List.of(new HellishRebuke()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        resolveRebukeCombat(player2);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The granted ability also triggers from noncombat damage")
    void punishesNoncombatDamageToCaster() {
        harness.setHand(player1, List.of(new HellishRebuke()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        Permanent pinger = addCreatureReady(player2, new ProdigalSorcerer());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Prodigal Sorcerer");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The effect lasts only through the end of turn")
    void grantWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new HellishRebuke()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        Permanent pinger = addCreatureReady(player2, new ProdigalSorcerer());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(pinger);
        harness.assertLife(player1, 19);
    }

    private void resolveRebukeCombat(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
