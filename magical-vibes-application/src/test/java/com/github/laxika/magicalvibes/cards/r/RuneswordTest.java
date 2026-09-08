package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuneswordTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the targeted attacking creature")
    void boostsAttackingCreature() {
        Permanent attacker = addAttacker(player1, new GiantSpider());
        Permanent sword = addReady(player1, new Runesword());

        activate(sword, attacker);

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Exiles creatures damaged by the targeted creature and prevents regeneration")
    void exilesCreaturesDamagedByTargetedCreature() {
        Permanent attacker = addAttacker(player1, new GiantSpider());
        Permanent sword = addReady(player1, new Runesword());
        Permanent blocker = addReady(player2, new DrudgeSkeletons());
        blocker.setRegenerationShield(1);

        activate(sword, attacker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertNotInGraveyard(player2, "Drudge Skeletons");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Drudge Skeletons"));
        assertThat(blocker.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifices itself when the targeted creature leaves the battlefield")
    void sacrificesWhenTargetLeaves() {
        Permanent attacker = addAttacker(player1, new GiantSpider());
        Permanent sword = addReady(player1, new Runesword());

        activate(sword, attacker);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, attacker));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runesword");
        harness.assertInGraveyard(player1, "Runesword");
    }

    @Test
    @DisplayName("Cannot target a nonattacking permanent")
    void cannotTargetNonattackingPermanent() {
        Permanent sword = addReady(player1, new Runesword());
        Permanent mountain = addReady(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, sword), 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activate(Permanent sword, Permanent attacker) {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(player1, sword), 0, null, attacker.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player player, Card card) {
        Permanent permanent = addReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
