package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.InterfaceAce;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OriginSpellbomb;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuidelightPathmaker.class, OriginSpellbomb.class, SolemnSimulacrum.class,
        InterfaceAce.class, Island.class})
class GuidelightPathmakerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability offers all artifact cards")
    void acceptingEtbAbilityOffersArtifacts() {
        setupAndCast();
        OriginSpellbomb lowManaValue = new OriginSpellbomb();
        SolemnSimulacrum highManaValue = new SolemnSimulacrum();
        setLibrary(lowManaValue, highManaValue);

        resolveMayAbility(true);

        PendingInteraction.LibrarySearch search =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(lowManaValue, highManaValue);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("An artifact with mana value 2 or less enters the battlefield")
    void lowManaValueArtifactEntersBattlefield() {
        setupAndCast();
        OriginSpellbomb lowManaValue = new OriginSpellbomb();
        setLibrary(lowManaValue);
        resolveMayAbility(true);

        chooseCard(lowManaValue);

        harness.assertOnBattlefield(player1, "Origin Spellbomb");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).doesNotContain(lowManaValue);
    }

    @Test
    @DisplayName("An artifact with mana value greater than 2 goes to hand")
    void highManaValueArtifactGoesToHand() {
        setupAndCast();
        SolemnSimulacrum highManaValue = new SolemnSimulacrum();
        setLibrary(highManaValue);
        resolveMayAbility(true);

        chooseCard(highManaValue);

        harness.assertInHand(player1, "Solemn Simulacrum");
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().equals(highManaValue));
    }

    @Test
    @DisplayName("Declining the ETB ability does not search")
    void decliningEtbAbilitySkipsSearch() {
        setupAndCast();
        setLibrary(new OriginSpellbomb());

        resolveMayAbility(false);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNull();
    }

    @Test
    void artifactAtExactCutoffEntersUntapped() {
        setupAndCast();
        InterfaceAce artifact = new InterfaceAce();
        setLibrary(artifact);
        resolveMayAbility(true);

        chooseCard(artifact);

        harness.assertOnBattlefield(player1, "Interface Ace");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().equals(artifact))
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isFalse());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    void searchExcludesNonartifacts() {
        setupAndCast();
        InterfaceAce artifact = new InterfaceAce();
        Island land = new Island();
        setLibrary(artifact, land);
        resolveMayAbility(true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(artifact);
        chooseCard(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void mayFailToFindEvenWhenArtifactIsAvailable() {
        setupAndCast();
        InterfaceAce artifact = new InterfaceAce();
        setLibrary(artifact);
        resolveMayAbility(true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        harness.assertNotOnBattlefield(player1, "Interface Ace");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptingSearchWithNoArtifactsCompletesWithoutSelection() {
        setupAndCast();
        Island land = new Island();
        setLibrary(land);

        resolveMayAbility(true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptingSearchWithEmptyLibraryCompletes() {
        setupAndCast();
        setLibrary();

        resolveMayAbility(true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void crewTapsSummoningSickCreatureAndAnimatesOnlyOnResolution() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new GuidelightPathmaker());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new SolemnSimulacrum());
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(vehicle.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(vehicle.isTapped()).isFalse();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new GuidelightPathmaker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void resolveMayAbility(boolean accept) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private void chooseCard(Card card) {
        int index = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().indexOf(card);
        harness.handleCardChosen(player1, index);
    }
}
