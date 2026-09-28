package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyboonEvangelist.class, GrizzlyBears.class})
class SkyboonEvangelistTest extends BaseCardTest {

    @Test
    void supportsUpToSixOtherCreatures() {
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        }
        harness.setHand(player1, List.of(new SkyboonEvangelist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creatures).allSatisfy(creature ->
                assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne());
    }

    @Test
    void grantsFlyingToCounteredCreatureAttackingOpponent() {
        addCreatureReady(player1, new SkyboonEvangelist());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotGrantFlyingWithoutCounter() {
        addCreatureReady(player1, new SkyboonEvangelist());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotGrantFlyingWhenAttackingController() {
        addCreatureReady(player1, new SkyboonEvangelist());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotGrantFlyingToCounteredCreatureAttackingPlaneswalker() {
        addCreatureReady(player1, new SkyboonEvangelist());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent planeswalker = addPlaneswalker(player2);

        declareAttackerAtPermanent(player1, attacker, planeswalker);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();
    }

    private void declareAttackerAtPermanent(Player attackerController, Permanent attacker, Permanent target) {
        harness.forceActivePlayer(attackerController);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(attackerController.getId()).indexOf(attacker);
        gs.declareAttackers(gd, attackerController, List.of(attackerIndex), Map.of(attackerIndex, target.getId()));
    }

    private Permanent addPlaneswalker(Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setColor(CardColor.BLUE);
        card.setLoyalty(3);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
