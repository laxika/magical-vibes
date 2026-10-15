package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MakeAStand;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TransmogrifyingWand.class, GrizzlyBears.class, Forest.class, MakeAStand.class})
class TransmogrifyingWandTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three charge counters")
    void entersWithThreeChargeCounters() {
        harness.setHand(player1, List.of(new TransmogrifyingWand()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent wand = findPermanent(player1, "Transmogrifying Wand");
        assertThat(wand.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Destroys a target creature, removes a charge counter, and creates an Ox for its controller")
    void destroysCreatureAndCreatesOxForItsController() {
        Permanent wand = addReadyWand(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(wand.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ox")
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getColor() == CardColor.WHITE
                        && permanent.getCard().getPower() == 2
                        && permanent.getCard().getToughness() == 4
                        && permanent.getCard().getSubtypes().contains(CardSubtype.OX));
    }

    @Test
    @DisplayName("Can target only creatures")
    void cannotTargetNonCreaturePermanent() {
        addReadyWand(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can be activated only at sorcery speed")
    void activationIsSorcerySpeedOnly() {
        addReadyWand(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void activationPaysCostsBeforeResolution() {
        Permanent wand = addReadyWand(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(wand.isTapped()).isTrue();
        assertThat(wand.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithoutChargeCounters() {
        Permanent wand = addReadyWand(player1);
        wand.setCounterCount(CounterType.CHARGE, 0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counters");
        assertThat(wand.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void canDestroyOwnCreatureAndCreateOxForSelf() {
        addReadyWand(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ox");
        harness.assertNotOnBattlefield(player2, "Ox");
    }

    @Test
    void illegalTargetCreatesNoOxAndDoesNotRefundCosts() {
        Permanent wand = addReadyWand(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ox");
        harness.assertNotOnBattlefield(player2, "Ox");
        assertThat(wand.isTapped()).isTrue();
        assertThat(wand.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void indestructibleCreatureSurvivesAndItsControllerStillCreatesOx() {
        addReadyWand(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MakeAStand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ox");
        harness.assertNotOnBattlefield(player2, "Ox");
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        addReadyWand(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
    private Permanent addReadyWand(Player player) {
        Permanent wand = harness.addToBattlefieldAndReturn(player, new TransmogrifyingWand());
        wand.setSummoningSick(false);
        wand.setCounterCount(CounterType.CHARGE, 3);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return wand;
    }
}
