package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.SealOfCleansing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NetterEnDal.class, SealOfCleansing.class})
class NetterEnDalTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card stops the target creature from attacking this turn")
    void discardLocksTargetCreatureFromAttacking() {
        addReadyNetter(player1);
        Permanent creature = addReadyNetter(player2);
        harness.setHand(player1, List.of(new SealOfCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttack(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        harness.assertInGraveyard(player1, "Seal of Cleansing");
    }

    @Test
    @DisplayName("The attack restriction wears off at end of turn")
    void attackRestrictionWearsOffAtEndOfTurn() {
        addReadyNetter(player1);
        Permanent creature = addReadyNetter(player2);
        harness.setHand(player1, List.of(new SealOfCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        gd.expireEndOfTurnFloatingEffects();

        assertThatCode(() -> declareAttack(creature)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyNetter(player1);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new SealOfCleansing());
        harness.setHand(player1, List.of(new SealOfCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyNetter(player1);
        Permanent creature = addReadyNetter(player2);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires white mana to activate")
    void requiresWhiteMana() {
        Permanent source = addReadyNetter(player1);
        Permanent target = addReadyNetter(player2);
        harness.setHand(player1, List.of(new SealOfCleansing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Seal of Cleansing");
    }

    @Test
    @DisplayName("Tapping the source is part of the activation cost")
    void tappingSourcePreventsAnotherActivation() {
        Permanent source = addReadyNetter(player1);
        Permanent target = addReadyNetter(player2);
        harness.setHand(player1, List.of(new SealOfCleansing(), new SealOfCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent addReadyNetter(Player player) {
        return addCreatureReady(player, new NetterEnDal());
    }

    private void declareAttack(Permanent creature) {
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        declareAttackers(player2, List.of(index));
    }
}
