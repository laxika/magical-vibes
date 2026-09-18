package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.t.TalasScout;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreechesBrazenPlunderer.class, TalasScout.class, GrizzlyBears.class,
        HermeticStudy.class, Divination.class, Forest.class})
class BreechesBrazenPlundererTest extends BaseCardTest {

    @Test
    @DisplayName("A Pirate dealing combat damage exiles the opponent's top card and grants play permission")
    void pirateCombatDamageExilesTopCard() {
        Card topCard = new Divination();
        Card nextCard = new Forest();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        addCreatureReady(player1, new BreechesBrazenPlunderer());
        addCreatureReady(player1, new TalasScout());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("The same trigger exiles only one card when multiple Pirates damage one opponent")
    void multiplePiratesAreBatched() {
        Card first = new Divination();
        Card second = new Forest();
        harness.setLibrary(player2, List.of(first, second));
        addCreatureReady(player1, new BreechesBrazenPlunderer());
        addCreatureReady(player1, new TalasScout());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Noncombat damage from a Pirate also grants the controller an any-color cast")
    void pirateNoncombatDamageGrantsAnyColorCast() {
        Permanent breeches = addCreatureReady(player1, new BreechesBrazenPlunderer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(breeches.getId());
        Divination topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("Damage from a non-Pirate does not trigger Breeches")
    void nonPirateDamageDoesNotTrigger() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(bears.getId());
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }
}
