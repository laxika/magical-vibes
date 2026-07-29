package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeliriumTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the target and it deals damage equal to its power to its controller")
    void tapsAndDealsPowerDamageToController() {
        harness.forceActivePlayer(player2);
        harness.setLife(player2, 20);
        Permanent target = addCreature(player2, 3, 3);

        castDelirium(target);

        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to and dealt by the target creature")
    void preventsCombatDamageToAndByTarget() {
        harness.forceActivePlayer(player2);
        Permanent target = addCreature(player2, 3, 3);

        castDelirium(target);

        assertThat(gd.creaturesWithCombatDamagePrevented).contains(target.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(target.getId());
    }

    @Test
    @DisplayName("Cannot be cast during the caster's own turn")
    void cannotBeCastOnOwnTurn() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreature(player2, 3, 3);
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature the active player does not control")
    void cannotTargetOwnCreature() {
        harness.forceActivePlayer(player2);
        Permanent own = addCreature(player1, 2, 2);
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, own.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new Delirium()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castDelirium(Permanent target) {
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player owner, int power, int toughness) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent perm = new Permanent(bears);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }
}
