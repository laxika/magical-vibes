package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SnoopingNewsie;
import com.github.laxika.magicalvibes.cards.s.SocialClimber;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CutOfTheProfits.class, SnoopingNewsie.class, SocialClimber.class})
class CutOfTheProfitsTest extends BaseCardTest {

    @Test
    void drawsAndLosesLifeEqualToX() {
        harness.setHand(player1, List.of(new CutOfTheProfits()));
        harness.setLibrary(player1, cards(6));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
    }

    @Test
    void casualtyCopiesTheSpell() {
        Permanent casualtyCreature = addCreatureReady(player1, new SocialClimber());
        harness.setHand(player1, List.of(new CutOfTheProfits()));
        harness.setLibrary(player1, cards(6));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 4);

        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(), false,
                casualtyCreature.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertLife(player1, 16);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    @Test
    void zeroXDrawsNothingAndLosesNoLife() {
        harness.setHand(player1, List.of(new CutOfTheProfits()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Cut of the Profits");
    }

    @Test
    void casualtySacrificeIsPaidBeforeResolutionAndCopyResolvesFirst() {
        Permanent creature = addCreatureReady(player1, new SocialClimber());
        harness.setHand(player1, List.of(new CutOfTheProfits()));
        harness.setLibrary(player1, cards(6));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 4);

        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(), false, creature.getId());

        harness.assertInGraveyard(player1, "Social Climber");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
        harness.assertNotInGraveyard(player1, "Cut of the Profits");

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertLife(player1, 16);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof CutOfTheProfits).hasSize(1);
    }

    @Test
    void casualtyRejectsCreatureBelowThreePower() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        harness.setHand(player1, List.of(new CutOfTheProfits()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 2, null, null,
                List.of(), List.of(), false, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void casualtyCannotSacrificeOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new SocialClimber());
        harness.setHand(player1, List.of(new CutOfTheProfits()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 2, null, null,
                List.of(), List.of(), false, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private List<Card> cards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new SnoopingNewsie())
                .toList();
    }
}
