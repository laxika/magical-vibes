package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WavecrashTriton.class, BronzeSable.class, LightningStrike.class})
class WavecrashTritonTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic taps an opponent's creature and locks its next untap")
    void heroicTapsAndLocksOpponentCreature() {
        UUID tritonId = harness.addToBattlefieldAndReturn(player1, new WavecrashTriton()).getId();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, tritonId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Heroic cannot target a creature you control")
    void heroicCannotTargetOwnCreature() {
        UUID tritonId = harness.addToBattlefieldAndReturn(player1, new WavecrashTriton()).getId();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, tritonId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger heroic")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new WavecrashTriton());
        harness.addToBattlefield(player2, new BronzeSable());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    void lockAppliesToAlreadyTappedCreatureForOnlyItsNextUntap() {
        UUID tritonId = harness.addToBattlefieldAndReturn(player1, new WavecrashTriton()).getId();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        bears.setTapped(true);
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, tritonId);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void opponentsSpellTargetingTritonDoesNotTriggerHeroic() {
        UUID tritonId = harness.addToBattlefieldAndReturn(player1, new WavecrashTriton()).getId();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, tritonId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(bears.isTapped()).isFalse();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void heroicResolvesBeforeTheTargetingSpell() {
        UUID tritonId = harness.addToBattlefieldAndReturn(player1, new WavecrashTriton()).getId();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, tritonId);
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Lightning Strike");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Lightning Strike");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void heroicStillResolvesAfterTritonLeavesTheBattlefield() {
        UUID tritonId = harness.addToBattlefieldAndReturn(player1, new WavecrashTriton()).getId();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.setHand(player2, List.of(new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player1, 0, tritonId);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.castAndResolveInstant(player2, 0, tritonId);
        harness.castAndResolveInstant(player2, 0, tritonId);
        harness.assertInGraveyard(player1, "Wavecrash Triton");

        harness.passBothPriorities();
        assertThat(bears.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void multipleHeroicTriggersDoNotPreventAdditionalUntapSteps() {
        UUID tritonId = harness.addToBattlefieldAndReturn(player1, new WavecrashTriton()).getId();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.setHand(player1, List.of(new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, tritonId);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, tritonId);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }
}
