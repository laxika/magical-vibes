package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeafcrownDryad;
import com.github.laxika.magicalvibes.cards.r.Rancor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DanithaNewBenaliasLight.class, Bonesplitter.class, GrizzlyBears.class, Rancor.class,
        LeafcrownDryad.class})
class DanithaNewBenaliasLightTest extends BaseCardTest {

    @Test
    void castsEquipmentFromGraveyard() {
        harness.addToBattlefield(player1, new DanithaNewBenaliasLight());
        harness.setGraveyard(player1, List.of(new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bonesplitter");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void castsAuraFromGraveyard() {
        harness.addToBattlefield(player1, new DanithaNewBenaliasLight());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Rancor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        prepareMainPhase(player1);

        harness.castFromGraveyardTargeting(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rancor");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
    }

    @Test
    void onlyOneAuraOrEquipmentMayBeCastEachTurn() {
        harness.addToBattlefield(player1, new DanithaNewBenaliasLight());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Bonesplitter(), new Rancor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonAuraOrEquipmentCannotBeCastFromGraveyard() {
        harness.addToBattlefield(player1, new DanithaNewBenaliasLight());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionIsUnavailableDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new DanithaNewBenaliasLight());
        harness.setGraveyard(player1, List.of(new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void failedManaPaymentDoesNotUsePermission() {
        harness.addToBattlefield(player1, new DanithaNewBenaliasLight());
        harness.setGraveyard(player1, List.of(new Bonesplitter()));
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Bonesplitter");
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bonesplitter");
    }

    @Test
    void equipmentMustFollowNormalTimingDuringYourTurn() {
        harness.addToBattlefield(player1, new DanithaNewBenaliasLight());
        harness.setGraveyard(player1, List.of(new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Bonesplitter");

        prepareMainPhase(player1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bonesplitter");
    }

    @Test
    void equipmentCannotBeCastWhileAnotherSpellIsOnStack() {
        harness.addToBattlefield(player1, new DanithaNewBenaliasLight());
        harness.setGraveyard(player1, List.of(new Bonesplitter()));
        prepareMainPhase(player1);
        harness.castFromHand(player1, new Bonesplitter(), "{1}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Bonesplitter");

        harness.passBothPriorities();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Bonesplitter"))
                .hasSize(2);
    }

    @Test
    void permissionRefreshesOnYourNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new DanithaNewBenaliasLight());
        harness.setGraveyard(player1, List.of(new Bonesplitter(), new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Bonesplitter"))
                .hasSize(2);
    }

    @Test
    void leavingBattlefieldRemovesPermissionButDoesNotStopCastSpell() {
        Permanent danitha = harness.addToBattlefieldAndReturn(player1, new DanithaNewBenaliasLight());
        harness.setGraveyard(player1, List.of(new Bonesplitter(), new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);
        harness.castFromGraveyard(player1, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, danitha));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bonesplitter");
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void newDanithaGrantsAnotherCastDuringSameTurn() {
        Permanent danitha = harness.addToBattlefieldAndReturn(player1, new DanithaNewBenaliasLight());
        harness.setGraveyard(player1, List.of(new Bonesplitter(), new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, danitha));
        harness.addToBattlefield(player1, new DanithaNewBenaliasLight());
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Bonesplitter"))
                .hasSize(2);
    }

    @Test
    void canCastBestowCardFromGraveyardAsAura() {
        Permanent danitha = harness.addToBattlefieldAndReturn(player1, new DanithaNewBenaliasLight());
        harness.setGraveyard(player1, List.of(new LeafcrownDryad()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        prepareMainPhase(player1);

        harness.castFromGraveyardTargeting(player1, 0, danitha.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Leafcrown Dryad");
        assertThat(findPermanent(player1, "Leafcrown Dryad").getAttachedTo()).isEqualTo(danitha.getId());
        assertThat(gqs.getEffectivePower(gd, danitha)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
