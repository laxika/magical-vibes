package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResplendentMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles a creature card and counters own creatures sharing its type")
    void enterExilesCreatureAndCountersSharedTypeCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        Permanent marshal = castMarshal();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(angel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(marshal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Accepting with multiple creature cards requires choosing one")
    void choosesOneCreatureCard() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears first = new GrizzlyBears();
        SerraAngel second = new SerraAngel();
        harness.setGraveyard(player1, List.of(first, second));

        castMarshal();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(second);
    }

    @Test
    @DisplayName("Declining the may ability does nothing")
    void decliningDoesNothing() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));

        castMarshal();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(graveyardCreature);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Death trigger excludes the Marshal from the card to exile")
    void deathTriggerExcludesItsOwnCard() {
        ResplendentMarshal sourceCard = new ResplendentMarshal();
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, sourceCard);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        SerraAngel graveyardAngel = new SerraAngel();
        harness.setGraveyard(player1, List.of(graveyardAngel));

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, marshal.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(graveyardAngel);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(sourceCard);
        assertThat(angel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Resplendent Marshal");
    }

    private Permanent castMarshal() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ResplendentMarshal()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Resplendent Marshal"))
                .findFirst()
                .orElseThrow();
    }
}
