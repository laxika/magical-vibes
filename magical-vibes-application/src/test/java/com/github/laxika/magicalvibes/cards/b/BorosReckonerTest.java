package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mugging;
import com.github.laxika.magicalvibes.cards.r.RuinationWurm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorosReckoner.class, GrizzlyBears.class, Shock.class, Mugging.class, RuinationWurm.class})
class BorosReckonerTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage: Reckoner deals that much damage to a chosen player")
    void nonCombatDamageReflectedToPlayer() {
        harness.addToBattlefield(player2, new BorosReckoner()); // 3/3
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID reckonerId = harness.getPermanentId(player2, "Boros Reckoner");
        harness.castAndResolveInstant(player1, 0, reckonerId);

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player2, "Boros Reckoner");
    }

    @Test
    @DisplayName("Combat damage: Reckoner reflects the combat damage it took at a chosen creature")
    void combatDamageReflectedToCreature() {
        Permanent reckoner = harness.addToBattlefieldAndReturn(player2, new BorosReckoner());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID otherBearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        reckoner.setSummoningSick(false);
        reckoner.setBlocking(true);
        reckoner.addBlockingTarget(0);

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
        Permanent reckoner = harness.addToBattlefieldAndReturn(player1, new BorosReckoner());
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

    @Test
    void lethalCombatDamageStillDealsTheFullAmount() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuinationWurm());
        Permanent reckoner = harness.addToBattlefieldAndReturn(player2, new BorosReckoner());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        reckoner.setBlocking(true);
        reckoner.addBlockingTarget(0);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boros Reckoner");
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertOnBattlefield(player1, "Ruination Wurm");
    }

    @Test
    void selfTargetedDamageTriggersAgainEvenWhenItKillsReckoner() {
        UUID reckonerId = harness.addToBattlefieldAndReturn(player1, new BorosReckoner()).getId();
        harness.setHand(player1, List.of(new Mugging()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);
        harness.castAndResolveSorcery(player1, 0, reckonerId);

        harness.handlePermanentChosen(player1, reckonerId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Boros Reckoner");

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void redManaCanActivateWhileTappedAndSummoningSick() {
        Permanent reckoner = harness.addToBattlefieldAndReturn(player1, new BorosReckoner());
        reckoner.setSummoningSick(true);
        reckoner.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, reckoner, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(reckoner.isTapped()).isTrue();
    }
}
