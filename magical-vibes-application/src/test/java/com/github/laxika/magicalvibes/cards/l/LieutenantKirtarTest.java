package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LieutenantKirtarTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability sacrifices Lieutenant Kirtar as a cost")
    void activatingSacrificesSelf() {
        Permanent kirtar = addReadyKirtar(player1);
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kirtar);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(kirtar.getCard());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Exiles the target attacking creature")
    void exilesAttackingCreature() {
        addReadyKirtar(player1);
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(attacker.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(attacker.getCard());
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addReadyKirtar(player1);
        Permanent creature = addCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    private Permanent addReadyKirtar(Player player) {
        Permanent kirtar = new Permanent(new LieutenantKirtar());
        kirtar.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(kirtar);
        return kirtar;
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player.getId()).add(attacker);
        return attacker;
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
