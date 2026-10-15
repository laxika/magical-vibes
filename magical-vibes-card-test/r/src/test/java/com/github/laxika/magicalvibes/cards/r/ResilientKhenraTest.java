package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.k.KhenraEternal;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({ResilientKhenra.class, KhenraEternal.class, Unsummon.class})
class ResilientKhenraTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may give target creature +X/+X equal to Khenra's power")
    void etbBoostsTargetBySourcePower() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());
        castAndAcceptMay(bears.getId());

        assertThat(bears.getEffectivePower()).isEqualTo(4); // 2 + 2
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may leaves the target unboosted")
    void decliningMaySkipsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ResilientKhenra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());
        castAndAcceptMay(bears.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Eternalize creates a 4/4 black Zombie token whose ETB can give +4/+4")
    void eternalizeTokenEtbBoostsByFour() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new ResilientKhenra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve eternalize -> target choice
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve triggered ability -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = findPermanent(player1, "Resilient Khenra");
        assertThat(token.getCard().isToken()).isTrue();

        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getManaCost()).isEmpty();
        assertThat(bears.getPowerModifier()).isEqualTo(4);
        assertThat(bears.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Eternalize exiles the source card from the graveyard as a cost")
    void eternalizeExilesSourceAsCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new ResilientKhenra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Resilient Khenra");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Resilient Khenra"));
    }

    @Test
    void canTargetItself() {
        castKhenra();
        Permanent khenra = findPermanent(player1, "Resilient Khenra");
        harness.handlePermanentChosen(player1, khenra.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(khenra.getPowerModifier()).isEqualTo(2);
        assertThat(khenra.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void usesSourcePowerAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());
        castKhenra();
        harness.handlePermanentChosen(player1, target.getId());
        findPermanent(player1, "Resilient Khenra").setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getPowerModifier()).isEqualTo(5);
        assertThat(target.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    void usesLastKnownPowerAfterSourceIsReturnedToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());
        castKhenra();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent source = findPermanent(player1, "Resilient Khenra");
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.assertNotOnBattlefield(player1, "Resilient Khenra");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getPowerModifier()).isEqualTo(5);
        assertThat(target.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    void eternalizedSourceUsesLastKnownPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());
        prepareEternalize();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent token = findPermanent(player1, "Resilient Khenra");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, token.getId());
        harness.assertNotOnBattlefield(player1, "Resilient Khenra");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    void removedTargetDoesNotReceiveBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());
        castKhenra();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void eternalizeCannotBeActivatedDuringCombat() {
        prepareEternalize();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Resilient Khenra");
    }

    @Test
    void eternalizeCannotBeActivatedWithAnAbilityOnTheStack() {
        prepareEternalize();
        harness.setGraveyard(player1, List.of(new ResilientKhenra(), new ResilientKhenra()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void eternalizeRequiresTwoGreenMana() {
        harness.setGraveyard(player1, List.of(new ResilientKhenra()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Resilient Khenra");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void prepareEternalize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new ResilientKhenra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void castAndAcceptMay(UUID targetId) {
        castKhenra();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void castKhenra() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ResilientKhenra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> target choice
    }
}
