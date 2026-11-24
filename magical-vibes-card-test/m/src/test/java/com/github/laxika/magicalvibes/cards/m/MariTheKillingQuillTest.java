package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.a.AssassinInitiate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MariTheKillingQuill.class, AssassinInitiate.class, GrizzlyBears.class, Murder.class})
class MariTheKillingQuillTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's creature dies and is exiled with a hit counter")
    void exilesOpponentCreatureWithHitCounter() {
        addCreatureReady(player1, new MariTheKillingQuill());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(victim.getCard());
        assertThat(gd.exiledCardsWithCounter(CounterType.HIT)).containsEntry(victim.getCard().getId(), 1);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not exile a creature card that left the graveyard and returned before the trigger resolves")
    void doesNotExileCardThatLeftAndReturnedToGraveyard() {
        addCreatureReady(player1, new MariTheKillingQuill());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");

        gd.playerGraveyards.get(player2.getId()).remove(victim.getCard());
        gd.playerGraveyards.get(player2.getId()).add(victim.getCard());
        gd.markGraveyardEntry(victim.getCard());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(victim.getCard());
        assertThat(gd.exiledCardsWithCounter(CounterType.HIT)).doesNotContainKey(victim.getCard().getId());
    }

    @Test
    @DisplayName("An outlaw's combat damage may remove a hit counter, draw, and create Treasures; a lone card is chosen unasked")
    void combatDamageUsesHitCounter() {
        addCreatureReady(player1, new MariTheKillingQuill());
        Permanent attacker = addCreatureReady(player1, new AssassinInitiate());
        attacker.setAttacking(true);
        Card exiled = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiled);
        gd.setExiledCardCounters(exiled.getId(), CounterType.HIT, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();

        assertThat(gd.exiledCardsWithCounter(CounterType.HIT)).doesNotContainKey(exiled.getId());
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Mari gives deathtouch to herself and the other outlaws you control only")
    void mariGrantsDeathtouchToHerselfAndOtherAssassinsOnly() {
        Permanent mari = addCreatureReady(player1, new MariTheKillingQuill());
        Permanent assassin = addCreatureReady(player1, new AssassinInitiate());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingAssassin = addCreatureReady(player2, new AssassinInitiate());

        assertThat(gqs.hasKeyword(gd, mari, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, assassin, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingAssassin, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Mari's own combat damage can use a hit counter")
    void marisOwnCombatDamageCanUseHitCounter() {
        Permanent mari = addCreatureReady(player1, new MariTheKillingQuill());
        mari.setAttacking(true);
        Card exiled = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiled);
        gd.setExiledCardCounters(exiled.getId(), CounterType.HIT, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.exiledCardsWithCounter(CounterType.HIT)).doesNotContainKey(exiled.getId());
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining to remove a hit counter gives no reward")
    void decliningHitCounterRemovalGivesNoReward() {
        addCreatureReady(player1, new MariTheKillingQuill());
        addCreatureReady(player1, new AssassinInitiate()).setAttacking(true);
        Card exiled = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiled);
        gd.setExiledCardCounters(exiled.getId(), CounterType.HIT, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.exiledCardsWithCounter(CounterType.HIT)).containsEntry(exiled.getId(), 1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("A hit counter on a card the damaged player doesn't own can't be used")
    void cannotUseHitCounterOnCardOwnedByAnotherPlayer() {
        addCreatureReady(player1, new MariTheKillingQuill());
        addCreatureReady(player1, new AssassinInitiate()).setAttacking(true);
        Card exiled = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiled);
        gd.setExiledCardCounters(exiled.getId(), CounterType.HIT, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCardsWithCounter(CounterType.HIT)).containsEntry(exiled.getId(), 1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Your own creature dying isn't exiled")
    void ownCreatureDeathDoesNotExileIt() {
        addCreatureReady(player1, new MariTheKillingQuill());
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, victim.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(victim.getCard());
        assertThat(gd.findExiledCard(victim.getCard().getId())).isNull();
        assertThat(gd.exiledCardsWithCounter(CounterType.HIT)).doesNotContainKey(victim.getCard().getId());
    }

    @Test
    @DisplayName("An exiled card without a hit counter gives no reward")
    void exiledCardWithoutHitCounterGivesNoReward() {
        addCreatureReady(player1, new MariTheKillingQuill());
        addCreatureReady(player1, new AssassinInitiate()).setAttacking(true);
        Card exiled = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiled);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Removes one hit counter, only from the chosen card, offering only the damaged player's cards")
    void removesOnlyOneHitCounterAndOnlyFromChosenCard() {
        addCreatureReady(player1, new MariTheKillingQuill());
        addCreatureReady(player1, new AssassinInitiate()).setAttacking(true);
        Card chosen = new GrizzlyBears();
        Card unchosen = new GrizzlyBears();
        Card wrongOwner = new GrizzlyBears();
        gd.addToExile(player2.getId(), chosen);
        gd.addToExile(player2.getId(), unchosen);
        gd.addToExile(player1.getId(), wrongOwner);
        gd.setExiledCardCounters(chosen.getId(), CounterType.HIT, 2);
        gd.setExiledCardCounters(unchosen.getId(), CounterType.HIT, 1);
        gd.setExiledCardCounters(wrongOwner.getId(), CounterType.HIT, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.cards()).extracting(Card::getId).containsExactlyInAnyOrder(chosen.getId(), unchosen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.exiledCardsWithCounter(CounterType.HIT)).containsEntry(chosen.getId(), 1)
                .containsEntry(unchosen.getId(), 1).containsEntry(wrongOwner.getId(), 1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }
}
