package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VorpalSword.class, GrizzlyBears.class})
class VorpalSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0 and deathtouch")
    void equippedCreatureGetsBonuses() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Activated ability makes the damaged player lose the game")
    void activatedAbilityMakesDamagedPlayerLose() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The granted ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        creature.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    private Permanent addSwordReady(Player player) {
        Permanent sword = new Permanent(new VorpalSword());
        sword.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sword);
        return sword;
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
