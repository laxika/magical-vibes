package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlertShuInfantry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlertShuInfantry.class, ChampionsVictory.class})
class ChampionsVictoryTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target attacking creature to its owner's hand")
    void returnsAttacker() {
        Permanent a1 = addCreatureReady(player1, new AlertShuInfantry());
        harness.setHand(player2, List.of(new ChampionsVictory()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        declareAttackers(player1, List.of(0));

        harness.castInstant(player2, 0, List.of(a1.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(a1.getCard());
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttacker() {
        addCreatureReady(player1, new AlertShuInfantry());
        Permanent idle = addCreatureReady(player2, new AlertShuInfantry());
        harness.setHand(player2, List.of(new ChampionsVictory()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.castInstant(player2, 0, List.of(idle.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        Permanent a1 = addCreatureReady(player1, new AlertShuInfantry());
        harness.setHand(player2, List.of(new ChampionsVictory()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        declareAttackers(player1, List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, List.of(a1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Does not return a creature that stops attacking before resolution")
    void doesNotReturnCreatureThatStopsAttackingBeforeResolution() {
        Permanent attacker = addCreatureReady(player1, new AlertShuInfantry());
        harness.setHand(player2, List.of(new ChampionsVictory()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        declareAttackers(player1, List.of(0));

        harness.castInstant(player2, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(attacker.getCard());
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("Cannot cast when only a planeswalker is attacked")
    void cannotCastWhenOnlyPlaneswalkerIsAttacked() {
        Permanent attacker = addCreatureReady(player1, new AlertShuInfantry());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player2, List.of(new ChampionsVictory()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
