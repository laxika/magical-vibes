package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaythFamedMechanist.class, BurnishedHart.class, SwordsToPlowshares.class})
class CaythFamedMechanistTest extends BaseCardTest {

    @Test
    void fabricatesItselfWithACounter() {
        Permanent cayth = castCayth(0);

        assertThat(cayth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void givesOtherNontokenCreaturesFabricate() {
        harness.addToBattlefield(player1, new CaythFamedMechanist());
        harness.setHand(player1, List.of(new BurnishedHart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        Permanent hart = findPermanent(player1, "Burnished Hart");
        harness.handleListChoice(player1, "Put a +1/+1 counter on this creature");
        resolveAllTriggers();

        assertThat(hart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activatedAbilityPopulates() {
        Permanent cayth = castCayth(1);
        cayth.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 0, null);
        resolveAllTriggers();

        assertThat(countPermanentsBySubtype(player1, CardSubtype.SERVO)).isEqualTo(2);
    }

    @Test
    void activatedAbilityProliferates() {
        Permanent cayth = castCayth(0);
        cayth.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 1, null);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(cayth.getId()));

        assertThat(cayth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void fabricateCreatesServoWhenCaythLeavesBeforeResolution() {
        prepareCayth();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent cayth = findPermanent(player1, "Cayth, Famed Mechanist");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, cayth.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cayth, Famed Mechanist");
        assertThat(countPermanentsBySubtype(player1, CardSubtype.SERVO)).isEqualTo(1);
    }

    @Test
    void grantedFabricateCanCreateServoInsteadOfCounter() {
        harness.addToBattlefield(player1, new CaythFamedMechanist());
        harness.setHand(player1, List.of(new BurnishedHart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();
        harness.handleListChoice(player1, "Create a 1/1 colorless Servo artifact creature token");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Burnished Hart")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanentsBySubtype(player1, CardSubtype.SERVO)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotGrantFabricateToOpponentsCreatures() {
        harness.addToBattlefield(player2, new CaythFamedMechanist());
        harness.setHand(player1, List.of(new BurnishedHart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Burnished Hart")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanentsBySubtype(player1, CardSubtype.SERVO)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void populateWithNoCreatureTokensCreatesNothing() {
        addCreatureReady(player1, new CaythFamedMechanist());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, 0, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void proliferateAddsEachCounterKindToChosenOpponentPermanentAndPlayer() {
        addCreatureReady(player1, new CaythFamedMechanist());
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        hart.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        hart.setCounterCount(CounterType.CHARGE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, 1, null);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(hart.getId(), player2.getId()));

        assertThat(hart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hart.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    void proliferateCanChooseNoPermanentsOrPlayers() {
        Permanent cayth = addCreatureReady(player1, new CaythFamedMechanist());
        cayth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, 1, null);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(cayth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castCayth(int mode) {
        prepareCayth();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();
        harness.handleListChoice(player1, mode == 0
                ? "Put a +1/+1 counter on this creature"
                : "Create a 1/1 colorless Servo artifact creature token");
        resolveAllTriggers();
        return findPermanent(player1, "Cayth, Famed Mechanist");
    }

    private void prepareCayth() {
        harness.setHand(player1, List.of(new CaythFamedMechanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private long countPermanentsBySubtype(com.github.laxika.magicalvibes.model.Player player,
                                          CardSubtype subtype) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(subtype))
                .count();
    }
}
