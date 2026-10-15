package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.v.VanquishTheWeak;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WarHistorian.class, InvasionOfInnistrad.class, VanquishTheWeak.class})
class WarHistorianTest extends BaseCardTest {

    @Test
    @DisplayName("Gains indestructible immediately when declared attacking a battle")
    void gainsIndestructibleWhenAttackingBattle() {
        Permanent battle = addBattle();
        Permanent historian = addCreatureReady(player1, new WarHistorian());

        attackBattle(historian, battle);

        assertThat(gqs.hasKeyword(gd, historian, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Keeps indestructible after combat and survives a destroy effect")
    void keepsIndestructibleForTheTurn() {
        Permanent battle = addBattle();
        Permanent historian = addCreatureReady(player1, new WarHistorian());

        attackBattle(historian, battle);
        resolveCombat();

        assertThat(gqs.hasKeyword(gd, historian, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, historian.getId());

        harness.assertOnBattlefield(player1, "War Historian");
    }

    @Test
    @DisplayName("Does not gain indestructible from attacking a player")
    void doesNotGainIndestructibleWhenAttackingPlayer() {
        Permanent historian = addCreatureReady(player1, new WarHistorian());

        declareAttackers(List.of(0));

        assertThat(gqs.hasKeyword(gd, historian, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Loses indestructible when the next turn begins")
    void losesIndestructibleNextTurn() {
        Permanent battle = addBattle();
        Permanent historian = addCreatureReady(player1, new WarHistorian());

        attackBattle(historian, battle);
        resolveCombat();
        assertThat(gqs.hasKeyword(gd, historian, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, historian, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Another War Historian does not gain indestructible from its ally's attack")
    void tracksBattleAttackForEachPermanent() {
        Permanent battle = addBattle();
        Permanent attacker = addCreatureReady(player1, new WarHistorian());
        Permanent nonattacker = addCreatureReady(player1, new WarHistorian());

        attackBattle(attacker, battle);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Can be destroyed before attacking a battle")
    void canBeDestroyedBeforeAttackingBattle() {
        addBattle();
        Permanent historian = addCreatureReady(player1, new WarHistorian());
        harness.setHand(player1, List.of(new VanquishTheWeak()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, historian.getId());

        harness.assertNotOnBattlefield(player1, "War Historian");
        harness.assertInGraveyard(player1, "War Historian");
    }

    private Permanent addBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        return battle;
    }

    private void attackBattle(Permanent historian, Permanent battle) {
        int historianIndex = gd.playerBattlefields.get(player1.getId()).indexOf(historian);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(historianIndex), Map.of(historianIndex, battle.getId()));
    }
}
