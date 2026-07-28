package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GiantTrapDoorSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability exiles both the attacker and the Spider")
    void exilesAttackerAndSelf() {
        Permanent spider = addReadySpider(player1);
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());
        payMana(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spider);
        assertThat(gd.exiledCards).extracting(e -> e.card().getId())
                .contains(attacker.getCard().getId(), spider.getCard().getId());
    }

    @Test
    @DisplayName("Activating the ability taps the Spider")
    void activatingTapsSpider() {
        Permanent spider = addReadySpider(player1);
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());
        payMana(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(spider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an attacking creature with flying")
    void cannotTargetFlyer() {
        addReadySpider(player1);
        Permanent flyer = addAttacker(player2, player1, new SuntailHawk());
        payMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without flying");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking you")
    void cannotTargetNonAttacker() {
        addReadySpider(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        payMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spider stays on the battlefield when the target leaves before resolution")
    void spiderSurvivesFizzle() {
        Permanent spider = addReadySpider(player1);
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());
        payMana(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spider);
        assertThat(gd.exiledCards).isEmpty();
    }

    private Permanent addReadySpider(Player player) {
        Permanent perm = new Permanent(new GiantTrapDoorSpider());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(controller.getId()).add(perm);
        return perm;
    }

    private void payMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
