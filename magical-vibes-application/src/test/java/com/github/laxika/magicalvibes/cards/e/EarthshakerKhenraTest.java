package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.k.KhenraEternal;
import com.github.laxika.magicalvibes.cards.h.HarrierNaga;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({EarthshakerKhenra.class, KhenraEternal.class, HarrierNaga.class, Unsummon.class})
class EarthshakerKhenraTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes a target creature with power <= Khenra's power unable to block")
    void etbMakesLowPowerTargetUnableToBlock() {
        Permanent blocker = addCreatureReady(player2, new KhenraEternal()); // power 2 == Khenra's power 2
        harness.setHand(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = blocker.getId();
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A higher-power creature is illegal, but Khenra can target itself")
    void cannotTargetHigherPowerCreature() {
        Permanent naga = addCreatureReady(player2, new HarrierNaga());
        harness.setHand(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, naga.getId()))
                .isInstanceOf(IllegalStateException.class);
        Permanent khenra = findPermanent(player1, "Earthshaker Khenra");
        harness.handlePermanentChosen(player1, khenra.getId());
        resolveAllTriggers();

        assertThat(khenra.isCantBlockThisTurn()).isTrue();
        assertThat(naga.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Eternalize creates a 4/4 black Zombie token copy with no mana cost")
    void eternalizeCreatesFourFourBlackZombieToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 6); // {4}{R}{R}

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Earthshaker Khenra");
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.JACKAL, CardSubtype.WARRIOR);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Eternalize exiles the source card from the graveyard as a cost")
    void eternalizeExilesSourceAsCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Earthshaker Khenra");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Earthshaker Khenra"));
    }

    @Test
    void eternalizeTokenCanPreventThreePowerCreatureFromBlocking() {
        Permanent naga = addCreatureReady(player2, new HarrierNaga());
        eternalize();
        harness.handlePermanentChosen(player1, naga.getId());
        resolveAllTriggers();

        assertThat(naga.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void eternalizeTriggerUsesLastKnownPowerAfterTokenIsBounced() {
        Permanent naga = addCreatureReady(player2, new HarrierNaga());
        eternalize();
        harness.handlePermanentChosen(player1, naga.getId());
        Permanent token = findPermanent(player1, "Earthshaker Khenra");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, token.getId());
        harness.assertNotOnBattlefield(player1, "Earthshaker Khenra");
        resolveAllTriggers();

        assertThat(naga.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void targetPowerIsRecheckedWhenTriggerResolves() {
        Permanent blocker = addCreatureReady(player2, new KhenraEternal());
        castKhenraAndPutTriggerOnStack(blocker.getId());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void sourcePowerIsRecheckedWhenTriggerResolves() {
        Permanent blocker = addCreatureReady(player2, new KhenraEternal());
        castKhenraAndPutTriggerOnStack(blocker.getId());
        findPermanent(player1, "Earthshaker Khenra").setPowerModifier(-1);
        resolveAllTriggers();

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void powerChangesAfterResolutionDoNotRestoreBlocking() {
        Permanent blocker = addCreatureReady(player2, new KhenraEternal());
        castKhenraAndPutTriggerOnStack(blocker.getId());
        resolveAllTriggers();
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        findPermanent(player1, "Earthshaker Khenra").setPowerModifier(-1);

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void blockingRestrictionExpiresAtEndOfTurn() {
        Permanent blocker = addCreatureReady(player2, new KhenraEternal());
        castKhenraAndPutTriggerOnStack(blocker.getId());
        resolveAllTriggers();
        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void eternalizeCannotBeActivatedDuringCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setGraveyard(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Earthshaker Khenra");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eternalizeCannotBeActivatedOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Earthshaker Khenra");
    }

    @Test
    void eternalizeCannotBeActivatedWithAnAbilityOnTheStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new EarthshakerKhenra(), new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 12);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void eternalizeRequiresTwoRedMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Earthshaker Khenra");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void eternalizeTokenCanAttackImmediately() {
        eternalize();
        Permanent token = findPermanent(player1, "Earthshaker Khenra");
        harness.handlePermanentChosen(player1, token.getId());
        resolveAllTriggers();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(token.isAttacking()).isTrue();
    }

    @Test
    void originalCanAttackImmediately() {
        harness.setHand(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent khenra = findPermanent(player1, "Earthshaker Khenra");
        harness.handlePermanentChosen(player1, khenra.getId());
        resolveAllTriggers();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(khenra.isAttacking()).isTrue();
    }
    private void castKhenraAndPutTriggerOnStack(UUID targetId) {
        harness.setHand(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void eternalize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
    }
}
