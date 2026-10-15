package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KishlaSkimmer.class, Forest.class, Reminisce.class, Shock.class})
class KishlaSkimmerTest extends BaseCardTest {

    @Test
    void drawsWhenACardLeavesYourGraveyardDuringYourTurn() {
        addKishlaSkimmer();
        seedLibrary(1);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new Reminisce()));

        castReminisce(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsOnlyOnceEachTurn() {
        addKishlaSkimmer();
        seedLibrary(3);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new Reminisce(), new Reminisce()));

        castReminisce(player1, player1.getId());
        harness.passBothPriorities();

        castReminisce(player1, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotConsumeTheTriggerDuringAnOpponentsTurn() {
        addKishlaSkimmer();
        seedLibrary(2);
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new Shock()));

        forceMainPhase(player2);
        harness.setHand(player2, List.of(new Reminisce()));
        addReminisceMana(player2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.setGraveyard(player1, List.of(new Shock()));
        forceMainPhase(player1);
        harness.setHand(player1, List.of(new Reminisce()));
        castReminisce(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsOnlyOneCardWhenSeveralCardsLeaveTogether() {
        addKishlaSkimmer();
        seedLibrary(2);
        harness.setGraveyard(player1, List.of(new Shock(), new Forest(), new KishlaSkimmer()));
        harness.setHand(player1, List.of(new Reminisce()));

        castReminisce(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawForAnOpponentsGraveyard() {
        addKishlaSkimmer();
        seedLibrary(2);
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new Reminisce()));

        castReminisce(player1, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotDrawWhenAnEmptyGraveyardIsShuffled() {
        addKishlaSkimmer();
        seedLibrary(2);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Reminisce()));

        castReminisce(player1, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachSkimmerDrawsIndependently() {
        addKishlaSkimmer();
        harness.addToBattlefield(player1, new KishlaSkimmer());
        seedLibrary(3);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new Reminisce()));

        castReminisce(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    private void addKishlaSkimmer() {
        harness.addToBattlefield(player1, new KishlaSkimmer());
        forceMainPhase(player1);
    }

    private void forceMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void seedLibrary(int count) {
        harness.setLibrary(player1, IntStream.range(0, count).mapToObj(i -> new Forest()).toList());
    }

    private void castReminisce(Player caster, UUID targetPlayerId) {
        addReminisceMana(caster);
        harness.castAndResolveSorcery(caster, 0, targetPlayerId);
    }

    private void addReminisceMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
