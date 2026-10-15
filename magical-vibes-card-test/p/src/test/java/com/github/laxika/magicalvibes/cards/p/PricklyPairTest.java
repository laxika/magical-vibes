package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PricklyPair.class})
class PricklyPairTest extends BaseCardTest {

    @Test
    @DisplayName("When Prickly Pair enters, it creates a Mercenary token")
    void enterTheBattlefieldCreatesMercenaryToken() {
        castPricklyPair();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
    }

    @Test
    @DisplayName("The Mercenary token boosts a creature you control")
    void mercenaryBoostsCreatureYouControl() {
        Permanent creature = addCreatureReady(player1, new PricklyPair());
        castPricklyPair();

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        prepareSorcerySpeedActivation();

        harness.activateAbility(player1, mercenaryIndex(mercenary), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Mercenary token cannot target an opposing creature")
    void mercenaryCannotTargetOpposingCreature() {
        Permanent opposingCreature = addCreatureReady(player2, new PricklyPair());
        castPricklyPair();

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        prepareSorcerySpeedActivation();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex(mercenary), 0, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("The Mercenary can only be activated at sorcery speed")
    void mercenaryRequiresSorcerySpeed() {
        Permanent creature = addCreatureReady(player1, new PricklyPair());
        castPricklyPair();

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex(mercenary), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The Mercenary can boost itself and the boost expires at end of turn")
    void mercenaryCanBoostItselfUntilEndOfTurn() {
        castPricklyPair();
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        prepareSorcerySpeedActivation();

        harness.activateAbility(player1, mercenaryIndex(mercenary), 0, null, mercenary.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mercenary)).isEqualTo(1);
        assertThat(mercenary.isTapped()).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(2);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mercenary)).isEqualTo(1);
    }

    @Test
    @DisplayName("A newly created Mercenary cannot pay its tap cost")
    void mercenaryCannotActivateWithSummoningSickness() {
        castPricklyPair();
        Permanent mercenary = findPermanent(player1, "Mercenary");
        prepareSorcerySpeedActivation();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex(mercenary), 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(mercenary.isTapped()).isFalse();
        assertThat(mercenary.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The Mercenary cannot activate during an opponent's main phase")
    void mercenaryCannotActivateOnOpponentsTurn() {
        castPricklyPair();
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex(mercenary), 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(mercenary.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The Mercenary cannot activate while a spell is on the stack")
    void mercenaryRequiresEmptyStack() {
        castPricklyPair();
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        prepareSorcerySpeedActivation();
        harness.setHand(player1, List.of(new PricklyPair()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex(mercenary), 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(mercenary.isTapped()).isFalse();
        resolveAllTriggers();
    }

    @Test
    @DisplayName("The created token is an untapped 1/1 red Mercenary creature")
    void createdTokenHasOracleCharacteristics() {
        castPricklyPair();
        Permanent mercenary = findPermanent(player1, "Mercenary");

        assertThat(mercenary.getCard().isToken()).isTrue();
        assertThat(mercenary.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(mercenary.getCard().getColors()).containsExactly(CardColor.RED);
        assertThat(mercenary.getCard().getSubtypes()).containsExactly(CardSubtype.MERCENARY);
        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mercenary)).isEqualTo(1);
        assertThat(mercenary.isTapped()).isFalse();
        assertThat(findPermanents(player2, "Mercenary")).isEmpty();
    }

    private void castPricklyPair() {
        harness.setHand(player1, List.of(new PricklyPair()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void prepareSorcerySpeedActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int mercenaryIndex(Permanent mercenary) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
    }
}
