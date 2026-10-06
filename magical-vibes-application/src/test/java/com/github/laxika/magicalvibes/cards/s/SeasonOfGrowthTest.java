package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.r.RabidBite;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonOfGrowth.class, GreenwoodSentinel.class, Shock.class, RabidBite.class,
        RaiseTheAlarm.class, Opalescence.class})
class SeasonOfGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Scry 1 when a creature you control enters")
    void scriesWhenYourCreatureEnters() {
        harness.addToBattlefield(player1, new SeasonOfGrowth());
        Card top = new Shock();
        Card bottom = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.setHand(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
    }

    @Test
    @DisplayName("Draws a card when you cast a spell targeting your creature")
    void drawsWhenSpellTargetsYourCreature() {
        harness.addToBattlefield(player1, new SeasonOfGrowth());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Card drawn = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Does not draw when the spell targets an opponent's creature")
    void doesNotDrawWhenSpellTargetsOpponentCreature() {
        harness.addToBattlefield(player1, new SeasonOfGrowth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Card libraryCard = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void canKeepScryedCardOnTop() {
        harness.addToBattlefield(player1, new SeasonOfGrowth());
        Card top = new Shock();
        Card bottom = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.setHand(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
    }

    @Test
    void doesNotScryWhenOpponentCreatureEnters() {
        harness.addToBattlefield(player1, new SeasonOfGrowth());
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player2, List.of(new GreenwoodSentinel()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void doesNotDrawWhenOpponentTargetsYourCreature() {
        harness.addToBattlefield(player1, new SeasonOfGrowth());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void doesNotDrawWhenSpellTargetsPlayer() {
        harness.addToBattlefield(player1, new SeasonOfGrowth());
        Card top = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void drawsOnceBeforeSpellWithMultipleTargetsResolves() {
        harness.addToBattlefield(player1, new SeasonOfGrowth());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Card first = new Shock();
        Card second = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, List.of(own.getId(), opponent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
    }

    @Test
    void scriesSeparatelyForEachCreatureToken() {
        harness.addToBattlefield(player1, new SeasonOfGrowth());
        Card first = new Shock();
        Card second = new GreenwoodSentinel();
        Card third = new RabidBite();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first, second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scriesForItsOwnEntryWhenItEntersAsCreature() {
        harness.addToBattlefield(player1, new Opalescence());
        Card top = new Shock();
        Card bottom = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.setHand(player1, List.of(new SeasonOfGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
    }
}
