package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.ShorelineLooter;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CalamitousTide.class, ShorelineLooter.class, Island.class})
class CalamitousTideTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two creatures, then draws two cards and discards a card")
    void returnsCreaturesThenDrawsAndDiscards() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());
        Card drawnFirst = new Island();
        Card drawnSecond = new Island();
        Card discard = new ShorelineLooter();
        harness.setLibrary(player1, List.of(drawnFirst, drawnSecond));
        harness.setHand(player1, List.of(new CalamitousTide(), discard));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discard, drawnFirst, drawnSecond);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnFirst, drawnSecond);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target only one creature")
    void returnsOneCreature() {
        Permanent returned = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new CalamitousTide(), new ShorelineLooter()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(returned.getId()));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new CalamitousTide()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawsAndDiscardsWithNoTargets() {
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new CalamitousTide()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDrawOrDiscardWhenAllChosenTargetsAreIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());
        Card held = new Island();
        Card first = new Island();
        Card second = new Island();
        CalamitousTide tide = new CalamitousTide();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(tide, held));
        addMana();
        harness.castSorcery(player1, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tide);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWhenOnlyOneOfTwoTargetsRemainsLegal() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new CalamitousTide()));
        harness.setHand(player2, List.of());
        addMana();
        harness.castSorcery(player1, 0, List.of(removed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.setGraveyard(player2, List.of(removed.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(removed.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void returnsStolenCreatureToItsOwnerAndCanDiscardReturnedCreature() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());
        gd.playerBattlefields.get(player2.getId()).remove(stolen);
        gd.playerBattlefields.get(player1.getId()).add(stolen);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ShorelineLooter());
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new CalamitousTide()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(stolen.getId(), own.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(stolen.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(own.getCard(), first, second);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(own.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotChooseThreeTargetsOrTheSameCreatureTwice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new ShorelineLooter());
        harness.setHand(player1, List.of(new CalamitousTide()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
