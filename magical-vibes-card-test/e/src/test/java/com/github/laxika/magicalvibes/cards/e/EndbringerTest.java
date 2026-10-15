package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Endbringer.class, Wastes.class, Humility.class})
class EndbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Endbringer untaps during each other player's untap step")
    void untapsDuringOtherPlayersUntapStep() {
        Permanent endbringer = addReadyEndbringer(player1);
        endbringer.tap();

        advanceToNextTurn(player1);

        assertThat(endbringer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Endbringer deals 1 damage to any target")
    void dealsDamageToAnyTarget() {
        Permanent endbringer = addReadyEndbringer(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(endbringer.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Endbringer can stop a creature from attacking or blocking")
    void targetCreatureCannotAttackOrBlock() {
        addReadyEndbringer(player1);
        Permanent target = addCreatureReady(player2, new Endbringer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantAttackThisTurn()).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(als.canAttack(gd, target, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Endbringer requires colorless mana for its restriction ability")
    void restrictionAbilityRequiresColorlessMana() {
        addReadyEndbringer(player1);
        Permanent target = addCreatureReady(player2, new Endbringer());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Endbringer draws a card")
    void drawsACard() {
        addReadyEndbringer(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Endbringer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Endbringer's restriction ability cannot target a noncreature")
    void restrictionAbilityCannotTargetNoncreature() {
        addReadyEndbringer(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Wastes());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void untapsOnlyItselfDuringOpponentsUntapStep() {
        Permanent endbringer = addReadyEndbringer(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Wastes());
        endbringer.tap();
        land.tap();

        harness.performUntapStep(player2);

        assertThat(endbringer.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @CardUsed({Endbringer.class, Humility.class})
    void doesNotUntapDuringOpponentsUntapStepAfterLosingAbilities() {
        Permanent endbringer = addReadyEndbringer(player1);
        harness.addToBattlefield(player2, new Humility());
        endbringer.tap();

        harness.performUntapStep(player2);

        assertThat(endbringer.isTapped()).isTrue();
    }

    @Test
    void damageAbilityCanTargetCreature() {
        addReadyEndbringer(player1);
        Permanent target = addReadyEndbringer(player2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void drawAbilityRequiresTwoColorlessMana() {
        Permanent endbringer = addReadyEndbringer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(endbringer.isTapped()).isFalse();
    }

    @Test
    void restrictionExpiresWhenTurnEnds() {
        addReadyEndbringer(player1);
        Permanent target = addReadyEndbringer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        advanceToNextTurn(player1);

        assertThat(target.isCantAttackThisTurn()).isFalse();
        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(als.canAttack(gd, target, player2.getId())).isTrue();
    }

    private Permanent addReadyEndbringer(Player player) {
        return addCreatureReady(player, new Endbringer());
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(currentActivePlayer == player1 ? player2 : player1, TurnStep.UPKEEP);
    }
}
