package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
import com.github.laxika.magicalvibes.cards.v.VoiceOfTheProvinces;
import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DescendantsPath.class, WanderingWolf.class, VoiceOfTheProvinces.class, AbundantGrowth.class})
class DescendantsPathTest extends BaseCardTest {

    @Test
    @DisplayName("Revealed creature sharing a creature type offers the free cast")
    void sharedTypeCreatureOffersFreeCast() {
        harness.addToBattlefield(player1, new DescendantsPath());
        addCreatureReady(player1, new WanderingWolf());
        setLibraryTop(new WanderingWolf());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Accepting casts the revealed creature without paying its mana cost")
    void acceptingCastsForFree() {
        harness.addToBattlefield(player1, new DescendantsPath());
        addCreatureReady(player1, new WanderingWolf());
        setLibraryTop(new WanderingWolf());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the free creature spell

        assertThat(countPermanents(player1, "Wandering Wolf")).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining puts the revealed card on the bottom of the library")
    void decliningBottomsTheCard() {
        harness.addToBattlefield(player1, new DescendantsPath());
        addCreatureReady(player1, new WanderingWolf());
        Card top = new WanderingWolf();
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isNotSameAs(top);
        assertThat(deck.getLast()).isSameAs(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Wandering Wolf")).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature sharing no creature type is bottomed with no choice")
    void unsharedTypeCreatureIsBottomed() {
        harness.addToBattlefield(player1, new DescendantsPath());
        addCreatureReady(player1, new WanderingWolf()); // Wolf
        Card top = new VoiceOfTheProvinces(); // Angel: no shared type
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top);
    }

    @Test
    @DisplayName("A non-creature card is bottomed with no choice")
    void nonCreatureIsBottomed() {
        harness.addToBattlefield(player1, new DescendantsPath());
        addCreatureReady(player1, new WanderingWolf());
        Card top = new AbundantGrowth();
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isNotSameAs(top);
        assertThat(deck.getLast()).isSameAs(top);
    }

    @Test
    @DisplayName("No creatures controlled means nothing is castable")
    void noCreaturesMeansNoFreeCast() {
        harness.addToBattlefield(player1, new DescendantsPath());
        Card top = new WanderingWolf();
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top);
    }

    @Test
    void opponentsMatchingCreatureDoesNotEnableCasting() {
        harness.addToBattlefield(player1, new DescendantsPath());
        addCreatureReady(player2, new WanderingWolf());
        Card top = new WanderingWolf();
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top);
    }

    @Test
    void matchingCreatureMustStillBeControlledWhenTriggerResolves() {
        harness.addToBattlefield(player1, new DescendantsPath());
        var wolf = addCreatureReady(player1, new WanderingWolf());
        Card top = new WanderingWolf();
        setLibraryTop(top);

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(wolf);
        gd.playerGraveyards.get(player1.getId()).add(wolf.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top);
    }

    @Test
    void emptyLibraryDoesNotOfferChoice() {
        harness.addToBattlefield(player1, new DescendantsPath());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new DescendantsPath());
        addCreatureReady(player1, new WanderingWolf());
        Card top = new WanderingWolf();
        setLibraryTop(top);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
    }

    @Test
    @CardUsed({GrafdiggersCage.class})
    void castingProhibitionBottomsMatchingCreature() {
        harness.addToBattlefield(player1, new DescendantsPath());
        harness.addToBattlefield(player2, new GrafdiggersCage());
        addCreatureReady(player1, new WanderingWolf());
        Card top = new WanderingWolf();
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top);
        assertThat(countPermanents(player1, "Wandering Wolf")).isEqualTo(1);
    }

    private void setLibraryTop(Card card) {
        harness.setLibrary(player1, List.of(card, new AbundantGrowth(), new AbundantGrowth(),
                new AbundantGrowth(), new AbundantGrowth()));
    }
}
