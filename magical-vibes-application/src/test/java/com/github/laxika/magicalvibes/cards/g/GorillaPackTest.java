package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GorillaPackTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no Forests")
    void sacrificedWhenNoForests() {
        harness.setHand(player1, List.of(new GorillaPack()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → state trigger fires
        harness.passBothPriorities(); // resolve state trigger → sacrificed

        harness.assertNotOnBattlefield(player1, "Gorilla Pack");
        harness.assertInGraveyard(player1, "Gorilla Pack");
    }

    @Test
    @DisplayName("Survives while controller controls a Forest")
    void survivesWithForest() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new GorillaPack()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gorilla Pack");
    }

    @Test
    @DisplayName("Can attack when defending player controls a Forest")
    void canAttackWhenDefenderControlsForest() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Forest()); // keep the pack alive
        harness.addToBattlefield(player2, new Forest());

        Permanent pack = new Permanent(new GorillaPack());
        pack.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pack);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Forest")
    void cannotAttackWhenDefenderHasNoForest() {
        harness.addToBattlefield(player1, new Forest()); // keep the pack alive

        Permanent pack = new Permanent(new GorillaPack());
        pack.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pack);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
