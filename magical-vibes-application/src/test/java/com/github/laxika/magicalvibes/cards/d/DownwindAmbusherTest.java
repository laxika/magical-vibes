package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DownwindAmbusher.class, GiantSpider.class, GrizzlyBears.class, Shock.class})
class DownwindAmbusherTest extends BaseCardTest {

    private static final String MINUS_ONE_MODE = "Target creature an opponent controls gets -1/-1 until end of turn";
    private static final String DESTROY_MODE = "Destroy target creature an opponent controls that was dealt damage this turn";

    @Test
    @DisplayName("Mode 0 gives an opposing creature -1/-1 until end of turn")
    void givesOpposingCreatureMinusOneMinusOne() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(MINUS_ONE_MODE, bears.getId());

        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mode 1 destroys an opposing creature dealt damage this turn")
    void destroysDamagedOpposingCreature() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, spider.getId());

        cast(DESTROY_MODE, spider.getId());

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Mode 1 cannot target an opposing creature that was not dealt damage this turn")
    void modeOneRequiresDamageThisTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new DownwindAmbusher());

        assertThatThrownBy(() -> harness.handleListChoice(player1,
                "Destroy target creature an opponent controls that was dealt damage this turn"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 0 cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new DownwindAmbusher());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1,
                "Target creature an opponent controls gets -1/-1 until end of turn");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destructionModeCanBeChosenWhenEnteringWithoutBeingCast() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, spider.getId());

        harness.enterBattlefieldAndReturn(player1, new DownwindAmbusher());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1,
                "Destroy target creature an opponent controls that was dealt damage this turn");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, spider.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    void minusOneMinusOneExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(MINUS_ONE_MODE, bears.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void canBeCastDuringOpponentsUpkeep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passUntil(player2, TurnStep.UPKEEP);

        cast(MINUS_ONE_MODE, bears.getId());

        harness.assertOnBattlefield(player1, "Downwind Ambusher");
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    void destructionModeCannotTargetOwnDamagedCreature() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, spider.getId());
        harness.enterBattlefieldAndReturn(player1, new DownwindAmbusher());

        assertThatThrownBy(() -> harness.handleListChoice(player1,
                "Destroy target creature an opponent controls that was dealt damage this turn"))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(String mode, UUID targetId) {
        harness.setHand(player1, List.of(new DownwindAmbusher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1, mode);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }
}
