package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CavalierOfThorns.class, Forest.class, Shock.class, WrathOfGod.class, Murder.class,
        LeylineOfTheVoid.class})
class CavalierOfThornsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a chosen land onto the battlefield and the rest into the graveyard")
    void etbPutsLandOntoBattlefieldAndRestIntoGraveyard() {
        Forest forest = new Forest();
        Forest mountain = new Forest();
        Shock shock1 = new Shock();
        Shock shock2 = new Shock();
        Shock shock3 = new Shock();
        setLibrary(shock1, forest, mountain, shock2, shock3);

        CavalierOfThorns cavalier = new CavalierOfThorns();
        castAndResolve(cavalier);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest, mountain);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().restToGraveyard()).isTrue();
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(shock1, mountain, shock2, shock3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB puts all five cards into the graveyard when no land is revealed")
    void etbPutsAllCardsIntoGraveyardWithoutLand() {
        Shock shock1 = new Shock();
        Shock shock2 = new Shock();
        Shock shock3 = new Shock();
        Shock shock4 = new Shock();
        Shock shock5 = new Shock();
        setLibrary(shock1, shock2, shock3, shock4, shock5);

        castAndResolve(new CavalierOfThorns());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(shock1, shock2, shock3, shock4, shock5);
    }

    @Test
    @DisplayName("Death trigger may exile Cavalier and excludes it from the target choices")
    void deathTriggerExilesSourceAndReturnsAnotherCardToLibraryTop() {
        CavalierOfThorns cavalier = new CavalierOfThorns();
        Card target1 = new Shock();
        Card target2 = new Forest();
        addCreatureReady(player1, cavalier);
        harness.setGraveyard(player1, List.of(target1, target2));
        castWrathOfGod();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(target1.getId()).doesNotContain(cavalier.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target1.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(cavalier.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(target1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target2);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void castAndResolve(Card card) {
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castWrathOfGod() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    void etbUsesAllAvailableCardsInAShortLibrary() {
        Forest land = new Forest();
        Shock spell = new Shock();
        setLibrary(spell, land);

        castAndResolve(new CavalierOfThorns());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(land);
                    assertThat(permanent.isTapped()).isFalse();
                });
    }

    @Test
    void etbWithAnEmptyLibraryDoesNothing() {
        setLibrary();

        castAndResolve(new CavalierOfThorns());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Cavalier of Thorns");
    }

    @Test
    void etbLeavesCardsBelowTheTopFiveInLibrary() {
        Forest land = new Forest();
        Shock first = new Shock();
        Shock second = new Shock();
        Shock third = new Shock();
        Shock fourth = new Shock();
        Forest sixth = new Forest();
        setLibrary(first, land, second, third, fourth, sixth);

        castAndResolve(new CavalierOfThorns());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third, fourth);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void etbWithoutLandRespectsLeylineOfTheVoid() {
        Shock first = new Shock();
        Shock second = new Shock();
        setLibrary(first, second);
        harness.addToBattlefield(player2, new LeylineOfTheVoid());

        castAndResolve(new CavalierOfThorns());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(first, second);
    }

    @Test
    void etbWithLandRespectsLeylineOfTheVoid() {
        Forest land = new Forest();
        Shock spell = new Shock();
        setLibrary(spell, land);
        harness.addToBattlefield(player2, new LeylineOfTheVoid());

        castAndResolve(new CavalierOfThorns());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(spell).doesNotContain(land);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void decliningDeathAbilityLeavesBothCardsInGraveyard() {
        CavalierOfThorns cavalier = new CavalierOfThorns();
        Shock target = new Shock();
        setLibrary();
        addCreatureReady(player1, cavalier);
        harness.setGraveyard(player1, List.of(target));
        castWrathOfGod();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cavalier, target);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(cavalier.getId()));
    }

    @Test
    void deathAbilityCannotExileSourceWithoutAnotherLegalTarget() {
        CavalierOfThorns cavalier = new CavalierOfThorns();
        addCreatureReady(player1, cavalier);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Cavalier of Thorns"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cavalier);
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(cavalier.getId()));
    }

    @Test
    void deathAbilityDoesNotReturnTargetWhenSourceHasLeftGraveyard() {
        CavalierOfThorns cavalier = new CavalierOfThorns();
        Shock target = new Shock();
        setLibrary();
        addCreatureReady(player1, cavalier);
        harness.setGraveyard(player1, List.of(target));
        castWrathOfGod();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(cavalier);
        gd.addToExile(player1.getId(), cavalier);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void deathAbilityWithAnIllegalTargetDoesNotExileSource() {
        CavalierOfThorns cavalier = new CavalierOfThorns();
        Shock target = new Shock();
        setLibrary();
        addCreatureReady(player1, cavalier);
        harness.setGraveyard(player1, List.of(target));
        castWrathOfGod();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        gd.addToExile(player1.getId(), target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cavalier);
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(cavalier.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
