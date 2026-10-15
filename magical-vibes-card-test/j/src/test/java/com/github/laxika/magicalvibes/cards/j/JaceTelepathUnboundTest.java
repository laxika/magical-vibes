package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.r.Recollect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaceVrynsProdigy.class, Shock.class, GrizzlyBears.class, LavaAxe.class, Negate.class, Recollect.class})
class JaceTelepathUnboundTest extends BaseCardTest {

    @Test
    @DisplayName("+1 shrinks the target by -2/-0 and the shrink outlasts end-of-turn cleanup")
    void plusOneShrinkOutlastsTheTurn() {
        Permanent jace = addJace(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isZero();
    }

    @Test
    @DisplayName("+1 may be activated with no target")
    void plusOneAllowsNoTarget() {
        Permanent jace = addJace(player1, 5);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 lets the targeted instant be cast from the graveyard later that turn, exiling it after")
    void minusThreeGrantsGraveyardCastThatExiles() {
        Permanent jace = addJace(player1, 5);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.activateAbility(player1, 0, 1, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        // The permission does not cast it — the card is still in the graveyard.
        harness.assertInGraveyard(player1, "Shock");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("-9 emblem mills the chosen opponent five cards whenever its controller casts a spell")
    void minusNineEmblemMillsOnSpellCast() {
        addJace(player1, 9);
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    void shrinkExpiresWhenControllersNextTurnBegins() {
        addJace(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId()));
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, bear)).isZero();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    void minusThreeSorceryStillRequiresMainPhaseAndFullManaCost() {
        addJace(player1, 5);
        Card axe = new LavaAxe();
        harness.setGraveyard(player1, List.of(axe));
        harness.activateAbility(player1, 0, 1, null, axe.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, 15);
        assertThat(gd.findExiledCard(axe.getId())).isNotNull();
    }

    @Test
    void minusThreeUnusedPermissionExpiresAtEndOfTurn() {
        addJace(player1, 5);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));
        harness.activateAbility(player1, 0, 1, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.findExiledCard(shock.getId())).isNull();
    }

    @Test
    void minusThreeRejectsCreatureAndOpponentsGraveyardCard() {
        Permanent jace = addJace(player1, 5);
        Card bear = new GrizzlyBears();
        Card opponentsShock = new Shock();
        harness.setGraveyard(player1, List.of(bear));
        harness.setGraveyard(player2, List.of(opponentsShock));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bear.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentsShock.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void minusThreePermissionSurvivesJaceDyingToLoyaltyCost() {
        addJace(player1, 3);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.activateAbility(player1, 0, 1, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jace, Telepath Unbound");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
    }

    @Test
    void minusThreeExilesSpellWhenCountered() {
        addJace(player1, 5);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.activateAbility(player1, 0, 1, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, shock.getId());

        harness.assertLife(player2, 20);
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    void minusThreeLosesTrackWhenTargetReturnsToHandBeforeBeingCast() {
        addJace(player1, 5);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.activateAbility(player1, 0, 1, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, shock.getId());
        harness.assertInHand(player1, "Shock");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.findExiledCard(shock.getId())).isNull();
    }

    @Test
    void emblemDoesNotTriggerForOpponentsSpells() {
        addJace(player1, 9);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addJace(Player player, int loyalty) {
        JaceVrynsProdigy card = new JaceVrynsProdigy();
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setTransformed(true);
        perm.setCard(card.getBackFaceCard());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

}
