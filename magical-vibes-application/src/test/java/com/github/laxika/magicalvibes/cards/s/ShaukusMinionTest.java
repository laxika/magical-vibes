package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShaukusMinionTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 2 damage to target white creature, killing a 2/1")
    void abilityKillsWhiteCreature() {
        Permanent minion = addMinionReady(player1);
        Permanent target = addCreature(player2, new SkyhunterSkirmisher());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(minion.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        harness.assertInGraveyard(player2, "Skyhunter Skirmisher");
    }

    @Test
    @DisplayName("Cannot target a non-white creature")
    void cannotTargetNonWhiteCreature() {
        addMinionReady(player1);
        Permanent bears = addCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
    }

    private Permanent addMinionReady(Player player) {
        Permanent minion = new Permanent(new ShaukusMinion());
        minion.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(minion);
        return minion;
    }

    private Permanent addCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
