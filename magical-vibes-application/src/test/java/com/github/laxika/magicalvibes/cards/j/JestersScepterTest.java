package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JestersScepter.class, GiantGrowth.class, GrizzlyBears.class, Shock.class})
class JestersScepterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by exiling the top five cards of the chosen player's library face down")
    void entersByExilingTopFiveCardsFaceDown() {
        JestersScepter scepterCard = new JestersScepter();
        harness.setHand(player1, List.of(scepterCard));
        harness.setLibrary(player2, List.of(
                new Shock(), new GiantGrowth(), new Shock(), new GiantGrowth(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent scepter = findPermanent(player1, "Jester's Scepter");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(scepter.getId())).hasSize(5);
        assertThat(gd.exiledCards.stream()
                .filter(entry -> scepter.getId().equals(entry.sourcePermanentId()))
                .toList())
                .allMatch(entry -> entry.faceDown() && player2.getId().equals(entry.ownerId()));
    }

    @Test
    @DisplayName("The enter-the-battlefield trigger resolves after the Scepter leaves")
    void enterTriggerResolvesAfterSourceLeaves() {
        JestersScepter scepterCard = new JestersScepter();
        harness.setHand(player1, List.of(scepterCard));
        harness.setLibrary(player2, List.of(
                new Shock(), new GiantGrowth(), new Shock(), new GiantGrowth(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent scepter = findPermanent(player1, "Jester's Scepter");
        gd.playerBattlefields.get(player1.getId()).remove(scepter);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(scepter.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Pays with a chosen exiled card and counters a spell with the same name")
    void paysWithChosenExiledCardAndCountersMatchingSpell() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new JestersScepter());
        Shock exiledShock = new Shock();
        GiantGrowth exiledGiantGrowth = new GiantGrowth();
        gd.addToExile(player2.getId(), exiledShock, scepter.getId());
        gd.addToExile(player2.getId(), exiledGiantGrowth, scepter.getId());

        Shock shockSpell = new Shock();
        harness.setHand(player2, List.of(shockSpell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shockSpell.getId());

        PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(exiledShock.getId(), exiledGiantGrowth.getId());

        harness.handleMultipleCardsChosen(player1, List.of(exiledShock.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledShock.getId())).isNull();
        assertThat(gd.getCardsExiledByPermanent(scepter.getId())).containsExactly(exiledGiantGrowth);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getId())
                .contains(exiledShock.getId(), shockSpell.getId());
        assertThat(scepter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can pay with a face-down card exiled by the Scepter")
    void paysWithFaceDownCardExiledByScepter() {
        JestersScepter scepterCard = new JestersScepter();
        Shock exiledShock = new Shock();
        GiantGrowth otherExiledCard = new GiantGrowth();
        harness.setHand(player1, List.of(scepterCard));
        harness.setLibrary(player2, List.of(exiledShock, otherExiledCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent scepter = findPermanent(player1, "Jester's Scepter");
        assertThat(gd.exiledCards.stream()
                .filter(entry -> scepter.getId().equals(entry.sourcePermanentId()))
                .toList())
                .allMatch(entry -> entry.faceDown());

        Shock shockSpell = new Shock();
        harness.setHand(player2, List.of(shockSpell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shockSpell.getId());

        PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds())
                .containsExactlyInAnyOrder(exiledShock.getId(), otherExiledCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(exiledShock.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledShock.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(exiledShock.getId(), shockSpell.getId());
    }

    @Test
    @DisplayName("The paid card still matches after the Scepter leaves the battlefield")
    void countersMatchingSpellAfterSourceLeaves() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new JestersScepter());
        Shock exiledShock = new Shock();
        gd.addToExile(player2.getId(), exiledShock, scepter.getId());
        Shock spell = new Shock();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, spell.getId());
        gd.playerBattlefields.get(player1.getId()).remove(scepter);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).contains(exiledShock.getId(), spell.getId());
    }

    @Test
    @DisplayName("A different matching card left in exile cannot replace the card paid")
    void doesNotCounterWhenPaidCardHasDifferentName() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new JestersScepter());
        Shock exiledShock = new Shock();
        GiantGrowth paidCard = new GiantGrowth();
        gd.addToExile(player2.getId(), exiledShock, scepter.getId());
        gd.addToExile(player2.getId(), paidCard, scepter.getId());
        Shock spell = new Shock();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, spell.getId());
        harness.handleMultipleCardsChosen(player1, List.of(paidCard.getId()));
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.getCardsExiledByPermanent(scepter.getId())).containsExactly(exiledShock);
        harness.assertInGraveyard(player2, "Giant Growth");
    }

    @Test
    @DisplayName("May target an unmatched spell but does not counter it")
    void mayTargetUnmatchedSpellWithoutCounteringIt() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new JestersScepter());
        Shock exiledShock = new Shock();
        gd.addToExile(player2.getId(), exiledShock, scepter.getId());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        GiantGrowth giantGrowth = new GiantGrowth();
        harness.setHand(player2, List.of(giantGrowth));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());

        harness.activateAbility(player1, 0, null, giantGrowth.getId());
        harness.handleMultipleCardsChosen(player1, List.of(exiledShock.getId()));
        resolveAllTriggers();

        assertThat(scepter.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        harness.assertInGraveyard(player2, "Giant Growth");
        harness.assertInGraveyard(player2, "Shock");
    }
}
