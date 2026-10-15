package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.z.ZimoneQuandrixProdigy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommandersInsight.class, ZimoneQuandrixProdigy.class})
class CommandersInsightTest extends BaseCardTest {

    @Test
    void targetPlayerDrawsXCardsWithoutCommanderCasts() {
        int targetHandSizeBefore = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new CommandersInsight()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(targetHandSizeBefore + 2);
    }

    @Test
    void targetPlayerDrawsAnAdditionalCardForEachCommanderCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Card commander = addCommanderToCommandZone();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        gd.stack.clear();
        gd.priorityPassedBy.clear();

        harness.setHand(player1, List.of(new CommandersInsight()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1;
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    void commanderCastsAreCountedForTheTargetPlayer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Card commander = addCommanderToCommandZone();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        gd.stack.clear();
        gd.priorityPassedBy.clear();

        int targetHandSizeBefore = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new CommandersInsight()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(targetHandSizeBefore + 2);
    }

    @Test
    void zeroXWithoutCommanderCastsDrawsNothing() {
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new CommandersInsight()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
        harness.assertInGraveyard(player1, "Commander's Insight");
    }

    @Test
    void zeroXDrawsForRepeatedCommandZoneCastsByOpponent() {
        Card commander = new ZimoneQuandrixProdigy();
        commander.setOwnerId(player2.getId());
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player2.getId(), commander);
        gd.playerCommandZones.put(player2.getId(), new ArrayList<>(List.of(commander)));
        castZimoneFromCommandZone(player2, commander, 0);

        gd.playerBattlefields.get(player2.getId()).clear();
        gd.playerCommandZones.get(player2.getId()).add(commander);
        castZimoneFromCommandZone(player2, commander, 2);

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new CommandersInsight()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    void castingCommanderFromHandDoesNotAddToDrawCount() {
        Card commander = new ZimoneQuandrixProdigy();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, commander, "{G}{U}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new CommandersInsight()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, 1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castZimoneFromCommandZone(Player player, Card commander, int tax) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, tax);
        gs.castCommander(gd, player, commander.getId(),
                () -> harness.castCreature(player, 0));
        harness.passBothPriorities();
    }

    private Card addCommanderToCommandZone() {
        Card commander = new Card();
        commander.setName("Test Commander");
        commander.setType(CardType.CREATURE);
        commander.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        commander.setManaCost("{1}");
        commander.setPower(2);
        commander.setToughness(2);
        commander.setOwnerId(player1.getId());
        commander.freeze();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }
}
