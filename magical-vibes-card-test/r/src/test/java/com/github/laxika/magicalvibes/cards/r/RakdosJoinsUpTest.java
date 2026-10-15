package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.o.OutcasterTrailblazer;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosJoinsUp.class, GrizzlyBears.class, IsamaruHoundOfKonda.class,
        DoomBlade.class, Shock.class, OutcasterTrailblazer.class, Opalescence.class})
class RakdosJoinsUpTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature from the graveyard with two +1/+1 counters")
    void returnsCreatureWithTwoCounters() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new RakdosJoinsUp(), "{3}{B}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals dying legendary creature's power to a target opponent")
    void dealsDyingLegendaryCreaturePowerToOpponent() {
        harness.addToBattlefield(player1, new RakdosJoinsUp());
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        legendary.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int opponentLife = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, legendary.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 3);
    }

    @Test
    @DisplayName("Does not trigger when a nonlegendary creature dies")
    void ignoresNonlegendaryCreatureDeath() {
        harness.addToBattlefield(player1, new RakdosJoinsUp());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int opponentLife = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void targetsOnlyCreatureCardsInControllersGraveyard() {
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        Card opponentsCreature = new IsamaruHoundOfKonda();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));

        harness.castFromHand(player1, new RakdosJoinsUp(), "{3}{B}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCreature);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void canEnterWithoutCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.castFromHand(player1, new RakdosJoinsUp(), "{3}{B}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rakdos Joins Up");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ignoresOpponentsLegendaryCreatureDeath() {
        harness.addToBattlefield(player1, new RakdosJoinsUp());
        Permanent legendary = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        int opponentLife = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, legendary.getId());

        harness.assertInGraveyard(player2, "Isamaru, Hound of Konda");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void doesNotReturnTargetThatLeftTheGraveyardBeforeResolution() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new RakdosJoinsUp(), "{3}{B}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Rakdos Joins Up");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void negativePowerLegendaryCreatureStillTriggersButDealsNoDamage() {
        harness.addToBattlefield(player1, new RakdosJoinsUp());
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        legendary.setPowerModifier(-3);
        int opponentLife = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, legendary.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Isamaru, Hound of Konda");
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersForItsOwnDeathWhenItIsALegendaryCreature() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent rakdos = harness.addToBattlefieldAndReturn(player1, new RakdosJoinsUp());
        int opponentLife = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, rakdos.getId());
        harness.castAndResolveInstant(player1, 0, rakdos.getId());
        harness.castAndResolveInstant(player1, 0, rakdos.getId());

        harness.assertInGraveyard(player1, "Rakdos Joins Up");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 5);
    }

    @Test
    void returnedCreatureHasCountersWhenEntryTriggersCheckItsPower() {
        harness.addToBattlefield(player1, new OutcasterTrailblazer());
        Card creature = new GrizzlyBears();
        Card libraryCard = new RakdosJoinsUp();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, new RakdosJoinsUp(), "{3}{B}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
