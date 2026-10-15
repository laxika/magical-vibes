package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.c.ChaliceOfTheVoid;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RepurposingBay.class, AetherSpellbomb.class, ChaliceOfTheVoid.class,
        LlanowarElves.class, MindStone.class, Ornithopter.class})
class RepurposingBayTest extends BaseCardTest {

    @Test
    void sacrificesAnArtifactAndPutsAnArtifactWithManaValueOneHigherOntoTheBattlefield() {
        harness.addToBattlefield(player1, new RepurposingBay());
        Ornithopter ornithopter = new Ornithopter();
        harness.addToBattlefield(player1, ornithopter);
        AetherSpellbomb spellbomb = new AetherSpellbomb();
        harness.setLibrary(player1, List.of(spellbomb));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Ornithopter");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == spellbomb);
    }

    @Test
    void offersOnlyArtifactCardsWithTheMatchingManaValue() {
        harness.addToBattlefield(player1, new RepurposingBay());
        harness.addToBattlefield(player1, new Ornithopter());
        AetherSpellbomb spellbomb = new AetherSpellbomb();
        harness.setLibrary(player1, List.of(spellbomb, new MindStone(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(spellbomb);
    }

    @Test
    void cannotActivateWithoutAnotherArtifact() {
        harness.addToBattlefield(player1, new RepurposingBay());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    void canOnlyBeActivatedAtSorcerySpeed() {
        harness.addToBattlefield(player1, new RepurposingBay());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void usesTheChosenArtifactsNonzeroManaValueWhenMultipleSacrificesAreAvailable() {
        var bay = harness.addToBattlefieldAndReturn(player1, new RepurposingBay());
        var spellbomb = harness.addToBattlefieldAndReturn(player1, new AetherSpellbomb());
        harness.addToBattlefield(player1, new Ornithopter());
        MindStone stone = new MindStone();
        harness.setLibrary(player1, List.of(stone, new AetherSpellbomb(), new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbomb.getId());
        harness.assertInGraveyard(player1, "Aether Spellbomb");
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(bay.isTapped()).isTrue();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(stone);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == stone)
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isFalse());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(stone);
        harness.assertNotInHand(player1, "Mind Stone");
    }

    @Test
    void treatsXInTheSacrificedArtifactsManaCostAsZero() {
        harness.addToBattlefield(player1, new RepurposingBay());
        var chalice = harness.addToBattlefieldAndReturn(player1, new ChaliceOfTheVoid());
        chalice.setCounterCount(CounterType.CHARGE, 4);
        AetherSpellbomb spellbomb = new AetherSpellbomb();
        harness.setLibrary(player1, List.of(spellbomb, new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(spellbomb);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Chalice of the Void");
        harness.assertOnBattlefield(player1, "Aether Spellbomb");
    }

    @Test
    void mayFailToFindEvenWhenAMatchingArtifactExists() {
        harness.addToBattlefield(player1, new RepurposingBay());
        harness.addToBattlefield(player1, new Ornithopter());
        AetherSpellbomb spellbomb = new AetherSpellbomb();
        harness.setLibrary(player1, List.of(spellbomb));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Aether Spellbomb");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spellbomb);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutAMatchingArtifact() {
        harness.addToBattlefield(player1, new RepurposingBay());
        harness.addToBattlefield(player1, new Ornithopter());
        MindStone stone = new MindStone();
        harness.setLibrary(player1, List.of(stone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(stone);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeANonartifactOrAnOpponentsArtifact() {
        harness.addToBattlefield(player1, new RepurposingBay());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    void cannotActivateDuringCombatOnItsControllersTurn() {
        harness.addToBattlefield(player1, new RepurposingBay());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhenTapped() {
        var bay = harness.addToBattlefieldAndReturn(player1, new RepurposingBay());
        bay.tap();
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        var bay = harness.addToBattlefieldAndReturn(player1, new RepurposingBay());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bay.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileAnotherAbilityIsOnTheStack() {
        var bay = harness.addToBattlefieldAndReturn(player1, new RepurposingBay());
        harness.addToBattlefield(player1, new AetherSpellbomb());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, 1, null, null);
        assertThat(gd.stack).hasSize(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bay.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canActivateAndResolveWithAnEmptyLibrary() {
        var bay = harness.addToBattlefieldAndReturn(player1, new RepurposingBay());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bay.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
