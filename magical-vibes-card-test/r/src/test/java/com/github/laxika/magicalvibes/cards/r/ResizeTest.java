package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.b.BorealShelf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Resize.class, BorealCentaur.class, BorealDruid.class, BorealShelf.class})
class ResizeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +3/+3 until end of turn")
    void givesTargetCreaturePlusThreePlusThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        harness.setHand(player1, List.of(new Resize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        harness.setHand(player1, List.of(new Resize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Recover returns Resize to its owner's hand when paid")
    void recoverReturnsSourceToHandWhenPaid() {
        Card resize = new Resize();
        harness.setGraveyard(player1, List.of(resize));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(resize);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(resize);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(resize);
    }

    @Test
    @DisplayName("Recover exiles Resize when declined")
    void recoverExilesSourceWhenDeclined() {
        Card resize = new Resize();
        harness.setGraveyard(player1, List.of(resize));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(resize);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(resize);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(resize);
    }

    @Test
    @DisplayName("Recover does not trigger when an opponent's creature enters their graveyard")
    void recoverDoesNotTriggerForOpponentsCreature() {
        Card resize = new Resize();
        harness.setGraveyard(player1, List.of(resize));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(resize);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(resize);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(resize);
    }

    @Test
    @DisplayName("Recover does not trigger when a noncreature enters its controller's graveyard")
    void recoverDoesNotTriggerForNoncreature() {
        Card resize = new Resize();
        harness.setGraveyard(player1, List.of(resize));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new BorealShelf());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(resize);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(resize);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(resize);
    }

    @Test
    @DisplayName("Recover exiles Resize when the chosen payment cannot be paid")
    void recoverExilesSourceWhenPaymentIsUnaffordable() {
        Card resize = new Resize();
        harness.setGraveyard(player1, List.of(resize));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(resize);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(resize);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(resize);
    }

    @Test
    @DisplayName("An old recover trigger cannot exile Resize after it is recovered and cast again")
    void oldRecoverTriggerCannotExileRecastSource() {
        Card resize = new Resize();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(resize));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        });
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).contains(resize);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(resize);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(resize);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(resize);
    }
}
