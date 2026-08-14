package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmedResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the Equipment controlled to target attacking creature")
    void dealsDamageEqualToControlledEquipment() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new GiantSpider());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new StriderHarness());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.setHand(player2, List.of(new ArmedResponse()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new GiantSpider());
        Permanent bystander = new Permanent(new GiantSpider());
        bystander.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bystander);
        harness.setHand(player2, List.of(new ArmedResponse()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(controller.getId()).add(perm);
        return perm;
    }
}
