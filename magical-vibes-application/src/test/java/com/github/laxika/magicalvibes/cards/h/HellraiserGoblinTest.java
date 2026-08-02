package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HellraiserGoblinTest extends BaseCardTest {

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addCreature(Player owner, com.github.laxika.magicalvibes.model.Card card, boolean summoningSick) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(summoningSick);
        gd.playerBattlefields.get(owner.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("A creature its controller controls must attack while Hellraiser Goblin is out")
    void ownCreatureMustAttack() {
        harness.addToBattlefield(player1, new HellraiserGoblin());
        addCreature(player1, new GrizzlyBears(), false);

        beginDeclareAttackers(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Hellraiser Goblin itself must attack too")
    void selfMustAttack() {
        Permanent goblin = new Permanent(new HellraiserGoblin());
        goblin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(goblin);

        beginDeclareAttackers(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Grants haste to the controller's creatures, including itself")
    void grantsHaste() {
        harness.addToBattlefield(player1, new HellraiserGoblin());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Hellraiser Goblin"), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste to an opponent's creatures")
    void noHasteForOpponent() {
        harness.addToBattlefield(player1, new HellraiserGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Grizzly Bears"), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not force an opponent's creature to attack")
    void opponentsCreatureNotForced() {
        harness.addToBattlefield(player1, new HellraiserGoblin());
        Permanent bears = addCreature(player2, new GrizzlyBears(), false);

        beginDeclareAttackers(player2);

        gs.declareAttackers(gd, player2, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Without Hellraiser Goblin, a creature is free to stay back")
    void notForcedWithoutHellraiser() {
        Permanent bears = addCreature(player1, new GrizzlyBears(), false);

        beginDeclareAttackers(player1);

        gs.declareAttackers(gd, player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }
}
