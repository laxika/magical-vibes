package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LieutenantKirtar.class, DwarvenGrunt.class})
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
    @DisplayName("Requires both generic and white mana to activate")
    void requiresFullActivationCost() {
        Permanent kirtar = addReadyKirtar(player1);
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kirtar);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(kirtar.getCard());
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
    @DisplayName("Can target an attacking creature you control")
    void canTargetOwnAttackingCreature() {
        addReadyKirtar(player1);
        Permanent attacker = addAttacker(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(attacker.getCard());
    }

    @Test
    @DisplayName("Does not exile the target if it stops attacking before resolution")
    void doesNotExileTargetThatStopsAttacking() {
        addReadyKirtar(player1);
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(attacker.getCard());
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
        return addCreatureReady(player, new LieutenantKirtar());
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new DwarvenGrunt());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new DwarvenGrunt());
    }
}
