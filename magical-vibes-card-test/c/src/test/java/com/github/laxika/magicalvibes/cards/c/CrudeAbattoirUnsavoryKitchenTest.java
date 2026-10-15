package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.RED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrudeAbattoirUnsavoryKitchen.class, GrizzlyBears.class, WallOfAir.class})
class CrudeAbattoirUnsavoryKitchenTest extends BaseCardTest {

    @Test
    void crudeAbattoirDealsTwoDamageToTheChosenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfAir());

        castRoom(0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void unsavoryKitchenPerpetuallyBoostsAChosenCreatureCardInHand() {
        castRoom(1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfAir());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new CrudeAbattoirUnsavoryKitchen(), bears));
        harness.addMana(player1, RED, 1);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualCreatureCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Card modified = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(modified.getPower()).isEqualTo(4);
        assertThat(modified.getKeywords()).contains(Keyword.HASTE);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void crudeAbattoirRequiresATargetWhenACreatureIsAvailable() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfAir());
        castRoom(0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void unlockingAbattoirOnTheSameRoomTriggersKitchen() {
        castRoom(1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfAir());
        harness.addMana(player1, RED, 1);
        harness.unlockRoomDoor(player1, 0, 0);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()).getFirst().getPower()).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getKeywords()).contains(Keyword.HASTE);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void kitchenTriggerResolvesAfterTheRoomLeavesTheBattlefield() {
        castRoom(1);
        Permanent room = gd.playerBattlefields.get(player1.getId()).getFirst();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new CrudeAbattoirUnsavoryKitchen(), bears));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfAir());
        harness.addMana(player1, RED, 1);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(room);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualCreatureCardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getPower()).isEqualTo(4);
    }

    @Test
    void lockedKitchenDoesNotTriggerFromAbattoirDamage() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new CrudeAbattoirUnsavoryKitchen(), bears));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfAir());
        harness.addMana(player1, RED, 1);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getPower()).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void kitchenResolvesWithoutAChoiceWhenNoCreatureCardIsInHand() {
        castRoom(1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfAir());
        harness.setHand(player1, List.of(new CrudeAbattoirUnsavoryKitchen()));
        harness.addMana(player1, RED, 1);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }
    private void castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new CrudeAbattoirUnsavoryKitchen()));
        harness.addMana(player1, RED, doorIndex == 0 ? 1 : 3);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
    }

}
