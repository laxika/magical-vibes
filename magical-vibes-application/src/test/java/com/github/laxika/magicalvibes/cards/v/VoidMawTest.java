package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SacredArmory;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VoidMawTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature is exiled with Void Maw instead of entering a graveyard")
    void exilesAnotherCreatureWithSource() {
        Permanent maw = addMaw();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent armory = harness.addToBattlefieldAndReturn(player1, new SacredArmory());

        removeToGraveyard(bears);
        removeToGraveyard(armory);

        assertThat(gd.getCardsExiledByPermanent(maw.getId()))
                .extracting(Card::getId).containsExactly(bears.getCard().getId());
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sacred Armory");
    }

    @Test
    @DisplayName("Void Maw itself is not exiled by its replacement effect")
    void doesNotExileItself() {
        Permanent maw = addMaw();

        removeToGraveyard(maw);

        harness.assertInGraveyard(player1, "Void Maw");
        assertThat(gd.getCardsExiledByPermanent(maw.getId())).isEmpty();
    }

    @Test
    @DisplayName("Putting an exiled card into its owner's graveyard pays for the self-boost")
    void paysWithExiledCardAndBoostsUntilEndOfTurn() {
        Permanent maw = addMaw();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        removeToGraveyard(bears);
        UUID exiledCardId = gd.getCardsExiledByPermanent(maw.getId()).getFirst().getId();

        int mawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(maw);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, mawIndex, 0, exiledCardId, Zone.EXILE);

        assertThat(gd.findExiledCard(exiledCardId)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, maw)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, maw)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, maw)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, maw)).isEqualTo(5);
    }

    @Test
    @DisplayName("Activated ability is available only while a card is exiled with Void Maw")
    void abilityAvailabilityRequiresExiledCard() {
        Permanent maw = addMaw();

        assertThat(gs.canActivateAbility(gd, player1.getId(), maw, 0,
                gd.playerManaPools.get(player1.getId()))).isFalse();

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        removeToGraveyard(bears);

        assertThat(gs.canActivateAbility(gd, player1.getId(), maw, 0,
                gd.playerManaPools.get(player1.getId()))).isTrue();
        assertThat(gd.getCardsExiledByPermanent(maw.getId()))
                .extracting(Card::getId)
                .containsExactly(bears.getCard().getId());
    }

    private Permanent addMaw() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new VoidMaw());
        maw.setSummoningSick(false);
        return maw;
    }

    private void removeToGraveyard(Permanent permanent) {
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
    }
}
