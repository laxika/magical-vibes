package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.cards.w.WingsOfHope;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntingKavu.class, RazorfootGriffin.class, WingsOfHope.class})
class HuntingKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability exiles both the attacker and Hunting Kavu")
    void exilesAttackerAndSelf() {
        Permanent kavu = addCreatureReady(player1, new HuntingKavu());
        Permanent attacker = addAttacker(player2, player1, new HuntingKavu());
        payMana(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kavu);
        assertThat(gd.exiledCards).extracting(e -> e.card().getId())
                .contains(attacker.getCard().getId(), kavu.getCard().getId());
    }

    @Test
    @DisplayName("Activating the ability taps Hunting Kavu")
    void activatingTapsKavu() {
        Permanent kavu = addCreatureReady(player1, new HuntingKavu());
        Permanent attacker = addAttacker(player2, player1, new HuntingKavu());
        payMana(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(kavu.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an attacking creature with flying")
    void cannotTargetFlyer() {
        addCreatureReady(player1, new HuntingKavu());
        Permanent flyer = addAttacker(player2, player1, new RazorfootGriffin());
        payMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without flying");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking you")
    void cannotTargetNonAttacker() {
        addCreatureReady(player1, new HuntingKavu());
        Permanent creature = addCreatureReady(player2, new HuntingKavu());
        payMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature attacking a different player")
    void cannotTargetCreatureAttackingDifferentPlayer() {
        addCreatureReady(player1, new HuntingKavu());
        Permanent attacker = addAttacker(player1, player2, new HuntingKavu());
        payMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking you");
    }

    @Test
    @DisplayName("Hunting Kavu stays on the battlefield when the target leaves before resolution")
    void kavuSurvivesFizzle() {
        Permanent kavu = addCreatureReady(player1, new HuntingKavu());
        Permanent attacker = addAttacker(player2, player1, new HuntingKavu());
        payMana(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kavu);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("The attacker is still exiled when Hunting Kavu leaves before resolution")
    void exilesAttackerWithoutSource() {
        Permanent kavu = addCreatureReady(player1, new HuntingKavu());
        Permanent attacker = addAttacker(player2, player1, new HuntingKavu());
        payMana(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(kavu);
        gd.playerGraveyards.get(player1.getId()).add(kavu.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.exiledCards).extracting(e -> e.card().getId())
                .containsExactly(attacker.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(kavu.getCard());
    }

    @Test
    @DisplayName("Neither creature is exiled if the attacker gains flying before resolution")
    void targetGainingFlyingMakesAbilityFizzle() {
        Permanent kavu = addCreatureReady(player1, new HuntingKavu());
        Permanent attacker = addAttacker(player2, player1, new HuntingKavu());
        payMana(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new WingsOfHope());
        aura.setAttachedTo(attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kavu);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker, aura);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Neither creature is exiled if the target stops attacking before resolution")
    void targetStoppingAttackMakesAbilityFizzle() {
        Permanent kavu = addCreatureReady(player1, new HuntingKavu());
        Permanent attacker = addAttacker(player2, player1, new HuntingKavu());
        payMana(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kavu);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.exiledCards).isEmpty();
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent perm = addCreatureReady(controller, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private void payMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
