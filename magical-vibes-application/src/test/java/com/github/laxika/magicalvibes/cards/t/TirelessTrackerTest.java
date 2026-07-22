package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TirelessTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall investigates — creates a Clue token when controller plays a land")
    void landfallInvestigates() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new TirelessTracker());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> clues = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Clue"))
                .toList();
        assertThat(clues).hasSize(1);
        Permanent clue = clues.getFirst();
        assertThat(clue.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(clue.getCard().getSubtypes()).contains(CardSubtype.CLUE);
        assertThat(clue.getCard().isToken()).isTrue();
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not investigate when opponent plays a land")
    void doesNotTriggerForOpponentLands() {
        harness.addToBattlefield(player1, new TirelessTracker());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Clue"));
    }

    @Test
    @DisplayName("Sacrificing a Clue puts a +1/+1 counter on Tireless Tracker")
    void clueSacrificePutsCounter() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new TirelessTracker());
        addClueToken(player1);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        int clueIndex = -1;
        for (int i = 0; i < bf.size(); i++) {
            if (bf.get(i).getCard().getName().equals("Clue")) {
                clueIndex = i;
                break;
            }
        }
        assertThat(clueIndex).isGreaterThanOrEqualTo(0);

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Clue"));
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a non-Clue permanent does not put a counter")
    void nonClueSacrificeDoesNotPutCounter() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new TirelessTracker());

        Card creature = new Card();
        creature.setName("Goblin Token");
        creature.setType(CardType.CREATURE);
        creature.setSubtypes(List.of(CardSubtype.GOBLIN));
        creature.setPower(1);
        creature.setToughness(1);
        creature.setToken(true);
        Permanent goblin = new Permanent(creature);
        goblin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(goblin);

        gd.playerBattlefields.get(player1.getId()).remove(goblin);
        gd.playerGraveyards.get(player1.getId()).add(goblin.getCard());
        harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player1.getId(), goblin.getCard());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addClueToken(Player player) {
        Card clueCard = new Card();
        clueCard.setName("Clue");
        clueCard.setType(CardType.ARTIFACT);
        clueCard.setManaCost("");
        clueCard.setToken(true);
        clueCard.setColor(null);
        clueCard.setSubtypes(List.of(CardSubtype.CLUE));
        clueCard.addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this token: Draw a card."
        ));
        Permanent clue = new Permanent(clueCard);
        clue.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(clue);
    }
}
