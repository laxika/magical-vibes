package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GuardianIdol;
import com.github.laxika.magicalvibes.cards.h.HornedHelm;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ArmedResponse.class, AuriokSalvagers.class, GuardianIdol.class, HornedHelm.class})
class ArmedResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the Equipment controlled to target attacking creature")
    void dealsDamageEqualToControlledEquipment() {
        Permanent attacker = addAttacker(player1, player2, new AuriokSalvagers());
        harness.addToBattlefield(player2, new HornedHelm());
        harness.addToBattlefield(player2, new HornedHelm());
        harness.addToBattlefield(player2, new GuardianIdol());
        harness.addToBattlefield(player1, new HornedHelm());
        prepareCast();

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Counts Equipment at resolution")
    void countsEquipmentAtResolution() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new HornedHelm());
        Permanent attacker = addAttacker(player1, player2, new AuriokSalvagers());
        prepareCast();

        harness.castInstant(player2, 0, attacker.getId());
        gd.playerBattlefields.get(player2.getId()).remove(equipment);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage a target that stops attacking before resolution")
    void targetThatStopsAttackingBeforeResolutionIsNotDamaged() {
        Permanent attacker = addAttacker(player1, player2, new AuriokSalvagers());
        harness.addToBattlefield(player2, new HornedHelm());
        prepareCast();

        harness.castInstant(player2, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addAttacker(player1, player2, new AuriokSalvagers());
        Permanent bystander = addCreatureReady(player1, new AuriokSalvagers());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    private void prepareCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new ArmedResponse()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent perm = addCreatureReady(controller, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}
