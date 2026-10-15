package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.l.LightningAxe;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Icefall.class, MishrasBauble.class, SnowCoveredForest.class, BorealDruid.class, LightningAxe.class})
class IcefallTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MishrasBauble());
        giveIcefall();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Mishra's Bauble");
    }

    @Test
    void destroysTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        giveIcefall();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Snow-Covered Forest");
    }

    @Test
    void cannotTargetCreatureThatIsNeitherArtifactNorLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealDruid());
        giveIcefall();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or land");
    }

    @Test
    void recoverReturnsIcefallToHandWhenPaid() {
        Card icefall = new Icefall();
        harness.setGraveyard(player1, List.of(icefall));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(icefall);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(icefall);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(icefall);
    }

    @Test
    void recoverExilesIcefallWhenDeclined() {
        Card icefall = new Icefall();
        harness.setGraveyard(player1, List.of(icefall));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(icefall);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(icefall);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(icefall);
    }

    @Test
    void recoverDoesNotTriggerForOpponentCreature() {
        Card icefall = new Icefall();
        harness.setGraveyard(player1, List.of(icefall));
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(icefall);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(icefall);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(icefall);
    }

    @Test
    void recoverDoesNotTriggerForNoncreaturePermanent() {
        Card icefall = new Icefall();
        harness.setGraveyard(player1, List.of(icefall));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(icefall);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(icefall);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(icefall);
    }

    @Test
    void recoverExilesIcefallWhenPaymentCannotBeMade() {
        Card icefall = new Icefall();
        harness.setGraveyard(player1, List.of(icefall));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(icefall);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(icefall);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(icefall);
    }

    @Test
    void oldRecoverTriggerCannotExileIcefallAfterItLeavesAndReturnsToGraveyard() {
        assertOldRecoverTriggerCannotMoveNewGraveyardObject(false);
    }

    @Test
    void oldRecoverTriggerCannotReturnIcefallAfterItLeavesAndReturnsToGraveyard() {
        assertOldRecoverTriggerCannotMoveNewGraveyardObject(true);
    }

    private void assertOldRecoverTriggerCannotMoveNewGraveyardObject(boolean pay) {
        Card icefall = new Icefall();
        harness.setGraveyard(player1, List.of(icefall));
        harness.setHand(player1, List.of(new LightningAxe()));
        Permanent firstDruid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        Permanent secondDruid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        Permanent opposingDruid = harness.addToBattlefieldAndReturn(player2, new BorealDruid());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firstDruid);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, secondDruid);
        });
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).contains(icefall);
        assertThat(gd.stack).hasSize(1);

        harness.castInstantWithDiscard(player1, 0, opposingDruid.getId(), 1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Boreal Druid");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, pay);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(icefall);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(icefall);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(icefall);
    }

    private void giveIcefall() {
        harness.setHand(player1, List.of(new Icefall()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
