package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.c.CrawGiant;
import com.github.laxika.magicalvibes.cards.d.DAvenantArcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KryShield.class, DAvenantArcher.class, CrawGiant.class, BarbaryApes.class})
class KryShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage by the target creature and gives it +0/+X")
    void preventsDamageAndBoostsToughness() {
        harness.setLife(player2, 20);
        addReadyShield(player1);
        Permanent archer = addReadyArcher(player1);

        activateShield(archer);

        assertThat(archer.getPowerModifier()).isZero();
        assertThat(archer.getToughnessModifier()).isEqualTo(3);

        attackWith(List.of(1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage by another creature")
    void doesNotPreventDamageByAnotherCreature() {
        harness.setLife(player2, 20);
        addReadyShield(player1);
        Permanent protectedArcher = addReadyArcher(player1);
        addReadyArcher(player1);

        activateShield(protectedArcher);

        attackWith(List.of(1, 2));

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents noncombat damage dealt by the target creature")
    void preventsNoncombatDamageByTargetCreature() {
        addReadyShield(player1);
        Permanent archer = addReadyArcher(player1);
        addReadyApe(player1);
        Permanent target = addReadyCrawGiant(player2);

        activateShield(archer);

        declareAttackers(List.of(2));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 2)));

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The boost and damage prevention expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent shield = addReadyShield(player1);
        Permanent archer = addReadyArcher(player1);

        activateShield(archer);
        assertThat(archer.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(archer.getPowerModifier()).isZero();
        assertThat(archer.getToughnessModifier()).isZero();
        assertThat(shield.isTapped()).isTrue();

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        attackWith(List.of(1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent shield = addReadyShield(player1);
        Permanent opponentCreature = addReadyArcher(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shield.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent you control")
    void cannotTargetOwnNoncreaturePermanent() {
        Permanent shield = addReadyShield(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shield.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shield.isTapped()).isFalse();
    }

    private Permanent addReadyShield(Player player) {
        Permanent shield = harness.addToBattlefieldAndReturn(player, new KryShield());
        shield.setSummoningSick(false);
        return shield;
    }

    private Permanent addReadyArcher(Player player) {
        return addCreatureReady(player, new DAvenantArcher());
    }

    private Permanent addReadyApe(Player player) {
        return addCreatureReady(player, new BarbaryApes());
    }

    private Permanent addReadyCrawGiant(Player player) {
        return addCreatureReady(player, new CrawGiant());
    }

    private void activateShield(Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private void attackWith(List<Integer> attackerIndices) {
        declareAttackersAndPrepareBlockers(attackerIndices);
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
