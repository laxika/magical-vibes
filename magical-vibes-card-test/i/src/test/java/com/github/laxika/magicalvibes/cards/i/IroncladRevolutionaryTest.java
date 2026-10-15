package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DefiantSalvager;
import com.github.laxika.magicalvibes.cards.l.LeaveInTheDust;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IroncladRevolutionary.class, Ornithopter.class, DefiantSalvager.class, LeaveInTheDust.class})
class IroncladRevolutionaryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB sacrifice puts two counters on Ironclad Revolutionary and makes each opponent lose 2 life")
    void etbSacrificeArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castIronclad();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        Permanent revolutionary = findPermanent(player1, "Ironclad Revolutionary");
        assertThat(revolutionary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Declining the ETB sacrifice does nothing")
    void declineSacrifice() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castIronclad();

        harness.handleMayAbilityChosen(player1, false);

        Permanent revolutionary = findPermanent(player1, "Ironclad Revolutionary");
        assertThat(revolutionary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Only an artifact can be sacrificed for the ETB ability")
    void nonArtifactCannotBeSacrificed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DefiantSalvager());
        castIronclad();

        harness.handleMayAbilityChosen(player1, true);

        Permanent revolutionary = findPermanent(player1, "Ironclad Revolutionary");
        assertThat(revolutionary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("An opponent's artifact cannot be sacrificed for the ability")
    void opponentsArtifactCannotBeSacrificed() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castIronclad();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Ironclad Revolutionary")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Sacrificing one artifact leaves the other artifact on the battlefield")
    void sacrificesOnlyChosenArtifact() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castIronclad();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remaining).doesNotContain(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chosen.getCard());
        assertThat(findPermanent(player1, "Ironclad Revolutionary")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Counters and life loss happen during the sacrifice ability's resolution")
    void rewardsResolveWithoutAnotherPriorityRound() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player2, List.of(new LeaveInTheDust()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        castIronclad();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(findPermanent(player1, "Ironclad Revolutionary")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life loss still happens if Ironclad Revolutionary leaves before its ability resolves")
    void lifeLossWithSourceAbsent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player2, List.of(new LeaveInTheDust()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new IroncladRevolutionary(), "{4}{B}{B}");
        harness.passBothPriorities();

        Permanent revolutionary = findPermanent(player1, "Ironclad Revolutionary");
        harness.castAndResolveInstant(player2, 0, revolutionary.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ironclad Revolutionary");
        harness.assertInHand(player1, "Ironclad Revolutionary");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    private void castIronclad() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new IroncladRevolutionary(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
