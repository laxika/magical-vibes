package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianNegatorTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage makes the Negator controller sacrifice that many permanents")
    void nonCombatDamageMakesControllerSacrificeThatManyPermanents() {
        harness.addToBattlefield(player2, new PhyrexianNegator());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID negatorId = harness.getPermanentId(player2, "Phyrexian Negator");
        harness.castInstant(player1, 0, negatorId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, p2Battlefield.subList(1, 3).stream()
                .map(Permanent::getId)
                .toList());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Phyrexian Negator");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Combat damage also makes the Negator controller sacrifice permanents")
    void combatDamageMakesControllerSacrificeThatManyPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new PhyrexianNegator());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent negator = gd.playerBattlefields.get(player2.getId()).getFirst();
        negator.setSummoningSick(false);
        negator.setBlocking(true);
        negator.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, p2Battlefield.subList(1, 3).stream()
                .map(Permanent::getId)
                .toList());

        harness.assertOnBattlefield(player2, "Phyrexian Negator");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
