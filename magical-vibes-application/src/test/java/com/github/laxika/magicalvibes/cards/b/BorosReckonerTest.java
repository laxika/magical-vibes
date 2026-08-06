package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BorosReckonerTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage: Reckoner deals that much damage to a chosen player")
    void nonCombatDamageReflectedToPlayer() {
        harness.addToBattlefield(player2, new BorosReckoner()); // 3/3
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID reckonerId = harness.getPermanentId(player2, "Boros Reckoner");
        harness.castInstant(player1, 0, reckonerId);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Boros Reckoner");
    }

    @Test
    @DisplayName("Combat damage: Reckoner reflects the combat damage it took at a chosen creature")
    void combatDamageReflectedToCreature() {
        harness.addToBattlefield(player2, new BorosReckoner()); // 3/3
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent reckoner = gd.playerBattlefields.get(player2.getId()).getFirst();
        reckoner.setSummoningSick(false);
        reckoner.setBlocking(true);
        reckoner.addBlockingTarget(0);

        UUID otherBearsId = gd.playerBattlefields.get(player1.getId()).get(1).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage: Reckoner takes 2

        harness.handlePermanentChosen(player2, otherBearsId);
        harness.passBothPriorities();

        // 2 damage is lethal for the untapped 2/2, and the blocked attacker died to 3 power
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("{R/W} grants first strike until end of turn, payable with white")
    void firstStrikeGrantedAndWearsOff() {
        harness.addToBattlefield(player1, new BorosReckoner());
        Permanent reckoner = gd.playerBattlefields.get(player1.getId()).getFirst();
        reckoner.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, reckoner, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, reckoner, Keyword.FIRST_STRIKE)).isFalse();
    }
}
