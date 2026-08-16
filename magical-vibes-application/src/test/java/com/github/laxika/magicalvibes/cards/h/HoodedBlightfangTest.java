package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HoodedBlightfangTest extends BaseCardTest {

    @Test
    void deathtouchAttackerCausesLifeLossAndLifeGain() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyBlightfang(player1);

        declareAttackers(player1, List.of(0), null);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void doesNotTriggerForNonDeathtouchAttacker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyBlightfang(player1);
        addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1), null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void deathtouchCombatDamageDestroysPlaneswalker() {
        addReadyBlightfang(player1);
        addReadyDeathtouchCreature(player1);
        Permanent planeswalker = addPlaneswalker(player2, 4);

        declareAttackers(player1, List.of(1), Map.of(1, planeswalker.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
    }

    @Test
    void deathtouchNoncombatDamageDestroysPlaneswalker() {
        addReadyBlightfang(player1);
        addReadyDeathtouchCreature(player1);
        Permanent planeswalker = addPlaneswalker(player2, 4);

        harness.activateAbility(player1, 1, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addReadyBlightfang(Player player) {
        return addReadyCreature(player, new HoodedBlightfang());
    }

    private Permanent addReadyDeathtouchCreature(Player player) {
        Card card = new ProdigalPyromancer();
        card.setKeywords(Set.of(Keyword.DEATHTOUCH));
        return addReadyCreature(player, card);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
