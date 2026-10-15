package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.t.TibaltRakishInstigator;
import com.github.laxika.magicalvibes.cards.p.PouncingLynx;
import com.github.laxika.magicalvibes.cards.l.LazotepPlating;
import com.github.laxika.magicalvibes.cards.d.DomriAnarchOfBolas;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NoEscape.class, LazotepPlating.class, TibaltRakishInstigator.class, PouncingLynx.class, DomriAnarchOfBolas.class})
class NoEscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell, exiles it, and starts scrying 1")
    void countersCreatureSpellAndExilesIt() {
        PouncingLynx bears = new PouncingLynx();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Pouncing Lynx");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Counters a planeswalker spell and scries 1")
    void countersPlaneswalkerSpell() {
        TibaltRakishInstigator chandra = new TibaltRakishInstigator();
        harness.setHand(player1, List.of(chandra));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, chandra.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Tibalt, Rakish Instigator");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker spell")
    void cannotTargetOtherSpell() {
        LazotepPlating opt = new LazotepPlating();
        harness.setHand(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, opt.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Completing the scry finishes resolving No Escape")
    void completingScryFinishesResolution() {
        PouncingLynx bears = new PouncingLynx();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.getGameService().handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Pouncing Lynx");
        harness.assertInGraveyard(player2, "No Escape");
    }

    @Test
    void scryCanPutExactlyOneCardOnBottomOfControllersLibrary() {
        PouncingLynx creature = new PouncingLynx();
        PouncingLynx top = new PouncingLynx();
        LazotepPlating next = new LazotepPlating();
        harness.setLibrary(player2, List.of(top, next));
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(next, top);
        harness.assertNotInGraveyard(player1, "Pouncing Lynx");
        harness.assertNotOnBattlefield(player1, "Pouncing Lynx");
        harness.assertInGraveyard(player2, "No Escape");
    }

    @Test
    void emptyLibraryDoesNotPreventCounteringOrFinishingResolution() {
        PouncingLynx creature = new PouncingLynx();
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "No Escape");
    }

    @Test
    void doesNotScryWhenTargetWasAlreadyCountered() {
        PouncingLynx creature = new PouncingLynx();
        PouncingLynx top = new PouncingLynx();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(top));
        harness.setHand(player1, List.of(creature, new NoEscape()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "No Escape");
    }

    @Test
    void stillScriesWhenCreatureCannotBeCounteredAndDoesNotExileIt() {
        harness.addToBattlefieldAndReturn(player1, new DomriAnarchOfBolas())
                .setCounterCount(CounterType.LOYALTY, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        PouncingLynx creature = new PouncingLynx();
        PouncingLynx top = new PouncingLynx();
        harness.setLibrary(player2, List.of(top));
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Pouncing Lynx");
        harness.assertNotInGraveyard(player1, "Pouncing Lynx");
        harness.assertInGraveyard(player2, "No Escape");
    }
}
