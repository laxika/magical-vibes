package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VizkopaGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability grants lifelink to the target creature")
    void grantsLifelink() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();

        int lifeBefore = gd.getLife(player1.getId());
        attackWith(bears);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20 - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("First ability cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        Permanent guildmage = gd.playerBattlefields.get(player1.getId()).getFirst();
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, guildmage, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Second ability drains each opponent for the life gained afterwards")
    void drainsOnLifeGain() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        addAbilityMana(player1);

        activateDrainWatcher();

        gainThreeLife(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20 - 3);
    }

    @Test
    @DisplayName("Second ability ignores life gained by an opponent")
    void ignoresOpponentLifeGain() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        addAbilityMana(player1);

        activateDrainWatcher();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gainThreeLife(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20 + 3);
    }

    @Test
    @DisplayName("Two activations of the second ability drain twice per life-gain event")
    void activationsStack() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        addAbilityMana(player1);
        addAbilityMana(player1);

        activateDrainWatcher();
        activateDrainWatcher();

        gainThreeLife(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20 - 6);
    }

    @Test
    @DisplayName("Second ability stops draining after the turn ends")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        addAbilityMana(player1);

        activateDrainWatcher();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gainThreeLife(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void activateDrainWatcher() {
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }

    /** Casts Angel of Mercy (ETB: gain 3 life) and resolves everything it puts on the stack. */
    private void gainThreeLife(Player player) {
        harness.setHand(player, List.of(new AngelOfMercy()));
        harness.addMana(player, ManaColor.WHITE, 5);
        harness.clearPriorityPassed();
        harness.castCreature(player, 0);
        resolveStack();
    }

    private void attackWith(Permanent creature) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        declareAttackers(player1, List.of(index));
        resolveCombat(player1);
    }

    private void resolveStack() {
        for (int i = 0; i < 10 && !gd.stack.isEmpty(); i++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
