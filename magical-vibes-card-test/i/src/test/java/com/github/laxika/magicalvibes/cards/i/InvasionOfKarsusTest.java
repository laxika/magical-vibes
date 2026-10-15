package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RefractionElemental;
import com.github.laxika.magicalvibes.cards.v.VolcanicSpite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarrukWildspeaker.class, GrizzlyBears.class, InvasionOfKarsus.class,
        RefractionElemental.class, VolcanicSpite.class})
class InvasionOfKarsusTest extends BaseCardTest {

    @Test
    @DisplayName("The Siege deals 3 damage to each creature and planeswalker when it enters")
    void etbDealsDamageToCreaturesAndPlaneswalkers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        castInvasion();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Defeating the Siege exiles it and casts Refraction Elemental transformed")
    void defeatCastsBackFace() {
        castInvasion();

        Permanent battle = findPermanent(player1, "Invasion of Karsus");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent backFace = findPermanent(player1, "Refraction Elemental");
        assertThat(backFace.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The back face deals 2 damage to each opponent whenever its controller casts a spell")
    void backFaceDamagesEachOpponentOnSpellCast() {
        InvasionOfKarsus front = new InvasionOfKarsus();
        harness.addToBattlefield(player1, front.getBackFaceCard());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int opponentLife = gd.getLife(player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 2);
    }

    private void castInvasion() {
        harness.castFromHand(player1, new InvasionOfKarsus(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void enteringDoesNotDamagePlayersOrBattles() {
        castInvasion();
        Permanent existingBattle = findPermanent(player1, "Invasion of Karsus");

        castInvasion();

        assertThat(existingBattle.getCounterCount(CounterType.DEFENSE)).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void backFaceTriggersForNoncreatureSpellBeforeThatSpellResolves() {
        harness.addToBattlefield(player1, new InvasionOfKarsus().getBackFaceCard());

        harness.castFromHand(player1, new InvasionOfKarsus(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Invasion of Karsus");
        harness.assertOnBattlefield(player1, "Refraction Elemental");
    }

    @Test
    void backFaceDoesNotTriggerForOpponentSpell() {
        harness.addToBattlefield(player1, new InvasionOfKarsus().getBackFaceCard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new InvasionOfKarsus(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Refraction Elemental");
    }

    @Test
    void wardCountersOpponentSpellWhenLifePaymentIsDeclined() {
        Permanent elemental = targetBackFaceWithOpponentSpell();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Volcanic Spite");
        harness.assertLife(player2, 20);
        assertThat(elemental.getMarkedDamage()).isZero();
    }

    @Test
    void wardPaymentCostsTwoLifeAndAllowsOpponentSpellToResolve() {
        Permanent elemental = targetBackFaceWithOpponentSpell();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(elemental.getMarkedDamage()).isEqualTo(3);
    }

    private Permanent targetBackFaceWithOpponentSpell() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1,
                new InvasionOfKarsus().getBackFaceCard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new VolcanicSpite()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, elemental.getId());
        return elemental;
    }
}
