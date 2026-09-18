package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WalkingBulwark.class, WallOfVines.class, GrizzlyBears.class})
class WalkingBulwarkTest extends BaseCardTest {

    @Test
    @DisplayName("The ability grants haste, attack permission, and toughness-based combat damage")
    void grantsAllAbilitiesAndAllowsImmediateAttack() {
        Permanent bulwark = addReady(player1, new WalkingBulwark());
        Permanent wall = addCreature(player1, new WallOfVines());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, bulwark), 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isEqualTo(3);

        declareAttackers(List.of(battlefieldIndex(player1, wall)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The granted abilities expire at end of turn")
    void grantedAbilitiesExpireAtEndOfTurn() {
        Permanent bulwark = addReady(player1, new WalkingBulwark());
        Permanent wall = addReady(player1, new WallOfVines());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, bulwark), 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isZero();
    }

    @Test
    @DisplayName("The ability only targets creatures with defender")
    void onlyTargetsDefenderCreatures() {
        Permanent bulwark = addReady(player1, new WalkingBulwark());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, bulwark), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with defender");
    }

    @Test
    @DisplayName("The ability can only be activated at sorcery speed")
    void onlyActivatesAtSorcerySpeed() {
        Permanent bulwark = addReady(player1, new WalkingBulwark());
        Permanent wall = addReady(player1, new WallOfVines());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, bulwark), 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
