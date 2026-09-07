package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({BogSerpent.class, Swamp.class, Forest.class})
class BogSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no Swamps")
    void sacrificedWhenNoSwamps() {
        harness.setHand(player1, List.of(new BogSerpent()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Serpent");
        harness.assertInGraveyard(player1, "Bog Serpent");
    }

    @Test
    @DisplayName("Survives while controller controls a Swamp")
    void survivesWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new BogSerpent()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Bog Serpent");
    }

    @Test
    @DisplayName("A non-Swamp land does not satisfy the state trigger")
    void nonSwampLandDoesNotSatisfyStateTrigger() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new BogSerpent()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Serpent");
        harness.assertInGraveyard(player1, "Bog Serpent");
    }

    @Test
    @DisplayName("Can attack when defending player controls a Swamp")
    void canAttackWhenDefenderControlsSwamp() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        Permanent serpent = addSerpent(player1);

        declareSerpentAttack(serpent);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Swamp")
    void cannotAttackWhenDefenderHasNoSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent serpent = addSerpent(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSerpent(Player player) {
        Permanent perm = new Permanent(new BogSerpent());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareSerpentAttack(Permanent serpent) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        gs.declareAttackers(gd, player1, List.of(index));
    }
}
