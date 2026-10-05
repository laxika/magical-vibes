package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
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

@CardUsed({KappaCannoneer.class, GlazeFiend.class, SolRing.class, SwordsToPlowshares.class})
class KappaCannoneerTest extends BaseCardTest {

    @Test
    void selfEntryResolvesBothInstructionsTogether() {
        harness.setHand(player1, List.of(new KappaCannoneer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent kappa = findPermanent(player1, "Kappa Cannoneer");
        assertThat(kappa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(kappa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void anotherArtifactEntryResolvesBothInstructionsTogether() {
        Permanent kappa = harness.addToBattlefieldAndReturn(player1, new KappaCannoneer());
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(kappa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isFalse();

        harness.passBothPriorities();

        assertThat(kappa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentArtifactEntryDoesNotTrigger() {
        Permanent kappa = harness.addToBattlefieldAndReturn(player1, new KappaCannoneer());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SolRing()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(kappa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void improvisePaysGenericManaWithAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.setHand(player1, List.of(new KappaCannoneer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()), false, null);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Kappa Cannoneer");
    }

    @Test
    void wardCountersOpponentSpellWithoutFourMana() {
        Permanent kappa = harness.addToBattlefieldAndReturn(player1, new KappaCannoneer());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, kappa.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kappa Cannoneer");
        harness.assertInGraveyard(player2, "Swords to Plowshares");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingFourManaForWardAllowsOpponentSpellToResolve() {
        Permanent kappa = harness.addToBattlefieldAndReturn(player1, new KappaCannoneer());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castInstant(player2, 0, kappa.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kappa Cannoneer");
    }

    @Test
    void controllerSpellDoesNotTriggerWard() {
        Permanent kappa = harness.addToBattlefieldAndReturn(player1, new KappaCannoneer());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, kappa.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kappa Cannoneer");
    }

    @Test
    @DisplayName("Kappa Cannoneer gets a counter and can't be blocked when it enters")
    void selfEntryTriggers() {
        harness.setHand(player1, List.of(new KappaCannoneer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent kappa = findPermanent(player1, "Kappa Cannoneer");
        assertThat(kappa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isTrue();
    }

    @Test
    @DisplayName("Another artifact entering gives Kappa Cannoneer a counter and unblockability")
    void anotherArtifactEntryTriggers() {
        Permanent kappa = harness.addToBattlefieldAndReturn(player1, new KappaCannoneer());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(kappa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isTrue();
    }

    @Test
    @DisplayName("Kappa Cannoneer's temporary unblockability wears off at cleanup")
    void unblockabilityWearsOffAtCleanup() {
        Permanent kappa = harness.addToBattlefieldAndReturn(player1, new KappaCannoneer());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isFalse();
        assertThat(kappa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
