package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DragonScarredBear;
import com.github.laxika.magicalvibes.cards.i.IxidorRealitySculptor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianShieldBearer.class, DragonScarredBear.class, IxidorRealitySculptor.class})
class GuardianShieldBearerTest extends BaseCardTest {

    @Test
    void megamorphPutsCountersOnItAndAnotherCreatureYouControl() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DragonScarredBear());
        Permanent guardian = castFaceDown();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guardian));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(otherCreature.getId());
        harness.handlePermanentChosen(player1, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void megamorphCounterIsPlacedBeforeTheTargetedTriggerResolves() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        Permanent guardian = castFaceDown();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guardian));

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, otherCreature.getId());
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void megamorphStillPutsCounterOnItWhenNoOtherCreatureIsControlled() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DragonScarredBear());
        Permanent guardian = castFaceDown();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guardian));

        assertThat(guardian.isFaceDown()).isFalse();
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turningFaceUpWithoutPayingMegamorphOnlyCountersTheOtherCreature() {
        Permanent ixidor = harness.addToBattlefieldAndReturn(player1, new IxidorRealitySculptor());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        Permanent guardian = castFaceDown();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ixidor),
                null, guardian.getId());
        harness.passBothPriorities();

        assertThat(guardian.isFaceDown()).isFalse();
        harness.handlePermanentChosen(player1, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ixidor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new GuardianShieldBearer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Guardian Shield-Bearer");
    }
}
