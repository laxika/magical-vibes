package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Splinterfright.class, AmbushViper.class, Plains.class})
class SplinterfrightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Splinterfright puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new Splinterfright(), "{2}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(Splinterfright.class);
    }

    @Test
    @DisplayName("Splinterfright is 0/0 with no creature cards in controller's graveyard")
    void isZeroZeroWithEmptyGraveyard() {
        Permanent perm = addSplinterfrightReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Splinterfright P/T equals number of creature cards in controller's graveyard")
    void ptEqualsCreatureCountInOwnGraveyard() {
        Permanent perm = addSplinterfrightReady(player1);
        harness.setGraveyard(player1, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Splinterfright does NOT count creature cards in opponent's graveyard")
    void doesNotCountOpponentsGraveyard() {
        Permanent perm = addSplinterfrightReady(player1);
        harness.setGraveyard(player2, createCreatureCards(4));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Splinterfright only counts creature cards, not non-creature cards")
    void onlyCountsCreatureCards() {
        Permanent perm = addSplinterfrightReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createCreatureCards(2));
        graveyard.add(new Plains());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Splinterfright mills 2 cards at controller's upkeep")
    void millsAtUpkeep() {
        addSplinterfrightReady(player1);
        // Keep Splinterfright alive when state-based actions are checked.
        harness.setGraveyard(player1, createCreatureCards(1));

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int graveyardSizeBefore = gd.playerGraveyards.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardSizeBefore + 2);
    }

    @Test
    @DisplayName("Mill trigger does NOT fire during opponent's upkeep")
    void millDoesNotFireDuringOpponentUpkeep() {
        addSplinterfrightReady(player1);
        harness.setGraveyard(player1, createCreatureCards(1));

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Milled creature cards increase Splinterfright's P/T")
    void milledCreaturesIncreasePT() {
        Permanent perm = addSplinterfrightReady(player1);
        harness.setGraveyard(player1, List.of(new AmbushViper()));

        // Put creature cards on top of library so they get milled
        harness.setLibrary(player1, List.of(new AmbushViper(), new AmbushViper(), new AmbushViper()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve mill trigger

        // 2 creature cards milled into graveyard
        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Splinterfright dies on resolution when its controller has no creature cards in the graveyard")
    void diesOnResolutionWithEmptyGraveyard() {
        Splinterfright card = new Splinterfright();
        harness.castFromHand(player1, card, "{2}{G}");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Splinterfright's defining ability works in hand and counts itself in the graveyard")
    void definingAbilityWorksOutsideBattlefield() {
        Splinterfright card = new Splinterfright();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(new AmbushViper(), new Plains()));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(1);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(card, new AmbushViper(), new Plains()));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(2);
    }

    @Test
    @DisplayName("Splinterfright's power and toughness decrease when creature cards leave the graveyard")
    void shrinksWhenCreaturesLeaveGraveyard() {
        Permanent perm = addSplinterfrightReady(player1);
        harness.setGraveyard(player1, createCreatureCards(3));
        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);

        harness.setGraveyard(player1, createCreatureCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);
    }

    @Test
    @DisplayName("The upkeep trigger mills the remaining card when the library contains only one card")
    void millsOnlyRemainingCard() {
        Permanent perm = addSplinterfrightReady(player1);
        harness.setGraveyard(player1, createCreatureCards(1));
        AmbushViper topCard = new AmbushViper();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Milling an empty library does not cause a loss")
    void millsEmptyLibraryWithoutLosing() {
        addSplinterfrightReady(player1);
        harness.setGraveyard(player1, createCreatureCards(1));
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The upkeep ability still mills after Splinterfright dies")
    void upkeepTriggerResolvesAfterSourceDies() {
        Permanent perm = addSplinterfrightReady(player1);
        harness.setGraveyard(player1, createCreatureCards(1));
        Plains first = new Plains();
        Plains second = new Plains();
        harness.setLibrary(player1, List.of(first, second, new Plains()));
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of());
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(perm);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(perm.getCard(), first, second);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addSplinterfrightReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Splinterfright());
        perm.setSummoningSick(false);
        return perm;
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new AmbushViper());
        }
        return creatures;
    }
}
