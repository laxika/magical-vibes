package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.w.WyluliWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuneboundWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Alone deals 1 damage to target opponent (counts itself)")
    void aloneDealsOne() {
        Permanent wolf = addReady(player1, new RuneboundWolf());
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counts another Wolf you control")
    void countsAnotherWolf() {
        addReady(player1, new RuneboundWolf());
        harness.addToBattlefield(player1, new WyluliWolf());
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts a Werewolf you control")
    void countsWerewolf() {
        addReady(player1, new RuneboundWolf());
        harness.addToBattlefield(player1, new GreaterWerewolf());
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not count a non-Wolf, non-Werewolf creature")
    void ignoresIrrelevantCreature() {
        addReady(player1, new RuneboundWolf());
        harness.addToBattlefield(player1, new GrizzlyBears());
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        addReady(player1, new RuneboundWolf());
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent wolf = new Permanent(new RuneboundWolf());
        wolf.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(wolf);
        addAbilityMana(player1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
