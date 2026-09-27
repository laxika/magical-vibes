package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({StalkingLeonin.class, GrizzlyBears.class})
class StalkingLeoninTest extends BaseCardTest {

    @Test
    @DisplayName("Revealing the chosen player exiles their creature attacking you")
    void exilesChosenPlayersAttackingCreature() {
        Permanent leonin = castStalkingLeonin();
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());
        chooseOpponent();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(leonin), null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(attacker.getCard().getId());
    }

    @Test
    @DisplayName("The ability rejects a creature that is not attacking you")
    void rejectsNonAttackingCreature() {
        Permanent leonin = castStalkingLeonin();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        chooseOpponent();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(leonin), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability can be activated only once")
    void activatesOnlyOnce() {
        Permanent leonin = castStalkingLeonin();
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());
        chooseOpponent();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(leonin), null, attacker.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(leonin), null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent castStalkingLeonin() {
        harness.setHand(player1, List.of(new StalkingLeonin()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Stalking Leonin");
    }

    private void chooseOpponent() {
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(controller.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
