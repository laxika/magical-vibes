package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({AyulaQueenAmongBears.class, GrizzlyBears.class, HillGiant.class, UniversalAutomaton.class,
        Bitterblossom.class, ArtificialEvolution.class})
class AyulaQueenAmongBearsTest extends BaseCardTest {

    @Test
    @DisplayName("A Bear entering lets Ayula put two counters on a target Bear")
    void putsCountersOnTargetBear() {
        harness.addToBattlefield(player1, new AyulaQueenAmongBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBear();

        harness.handleListChoice(player1, "Put two +1/+1 counters on target Bear.");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Bear entering lets Ayula make a Bear you control fight an opposing creature")
    void fightsTargetCreature() {
        harness.addToBattlefield(player1, new AyulaQueenAmongBears());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBear();

        harness.handleListChoice(player1,
                "Target Bear you control fights target creature you don't control.");
        harness.handlePermanentChosen(player1, ownBear.getId());
        harness.handlePermanentChosen(player1, opposingBear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingBear);
    }

    @Test
    @DisplayName("A non-Bear entering does not trigger Ayula")
    void nonBearDoesNotTrigger() {
        harness.addToBattlefield(player1, new AyulaQueenAmongBears());
        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castBear() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
    }

    @Test
    void ayulaDoesNotTriggerForHerOwnEntry() {
        harness.castFromHand(player1, new AyulaQueenAmongBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ayula, Queen Among Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsBearDoesNotTrigger() {
        harness.addToBattlefield(player1, new AyulaQueenAmongBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new UniversalAutomaton(), "{1}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Universal Automaton");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void changelingEntryCanPutCountersOnAyula() {
        Permanent ayula = harness.addToBattlefieldAndReturn(player1, new AyulaQueenAmongBears());
        harness.castFromHand(player1, new UniversalAutomaton(), "{1}");
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Put two +1/+1 counters on target Bear.");
        harness.handlePermanentChosen(player1, ayula.getId());
        harness.passBothPriorities();

        assertThat(ayula.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void enteringChangelingCanReceiveCounters() {
        harness.addToBattlefield(player1, new AyulaQueenAmongBears());
        harness.castFromHand(player1, new UniversalAutomaton(), "{1}");
        harness.passBothPriorities();
        Permanent enteringBear = findPermanent(player1, "Universal Automaton");

        harness.handleListChoice(player1, "Put two +1/+1 counters on target Bear.");
        harness.handlePermanentChosen(player1, enteringBear.getId());
        harness.passBothPriorities();

        assertThat(enteringBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void ayulaCanFightAndUsesHerOwnPower() {
        Permanent ayula = harness.addToBattlefieldAndReturn(player1, new AyulaQueenAmongBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new UniversalAutomaton());
        harness.castFromHand(player1, new UniversalAutomaton(), "{1}");
        harness.passBothPriorities();

        harness.handleListChoice(player1,
                "Target Bear you control fights target creature you don't control.");
        harness.handlePermanentChosen(player1, ayula.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ayula);
        assertThat(ayula.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        harness.assertInGraveyard(player2, "Universal Automaton");
    }

    @Test
    void neitherCreatureDealsFightDamageIfOneTargetLeaves() {
        Permanent ayula = harness.addToBattlefieldAndReturn(player1, new AyulaQueenAmongBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new UniversalAutomaton());
        harness.castFromHand(player1, new UniversalAutomaton(), "{1}");
        harness.passBothPriorities();

        harness.handleListChoice(player1,
                "Target Bear you control fights target creature you don't control.");
        harness.handlePermanentChosen(player1, ayula.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(opposingCreature);
        harness.passBothPriorities();

        assertThat(ayula.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ayula);
    }

    @Test
    void countersResolveAfterAyulaLeaves() {
        Permanent ayula = harness.addToBattlefieldAndReturn(player1, new AyulaQueenAmongBears());
        harness.castFromHand(player1, new UniversalAutomaton(), "{1}");
        harness.passBothPriorities();
        Permanent enteringBear = findPermanent(player1, "Universal Automaton");

        harness.handleListChoice(player1, "Put two +1/+1 counters on target Bear.");
        harness.handlePermanentChosen(player1, enteringBear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ayula);
        harness.passBothPriorities();

        assertThat(enteringBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void noncreatureKindredBearCanReceiveCounters() {
        harness.addToBattlefield(player1, new AyulaQueenAmongBears());
        Permanent kindredBear = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        changeFaerieToBear(kindredBear.getId());
        harness.castFromHand(player1, new UniversalAutomaton(), "{1}");
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Put two +1/+1 counters on target Bear.");
        harness.handlePermanentChosen(player1, kindredBear.getId());
        harness.passBothPriorities();

        assertThat(kindredBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void noncreatureKindredBearEnteringTriggersAyula() {
        Permanent ayula = harness.addToBattlefieldAndReturn(player1, new AyulaQueenAmongBears());
        harness.castFromHand(player1, new Bitterblossom(), "{1}{B}");
        UUID spellId = gd.stack.getFirst().getCard().getId();
        changeFaerieToBear(spellId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Put two +1/+1 counters on target Bear.");
        harness.handlePermanentChosen(player1, ayula.getId());
        harness.passBothPriorities();

        assertThat(ayula.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void changeFaerieToBear(UUID targetId) {
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "BEAR");
    }
}
