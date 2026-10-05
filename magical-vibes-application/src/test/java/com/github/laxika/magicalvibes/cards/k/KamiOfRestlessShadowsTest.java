package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DeathcultRogue;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MoonCircuitHacker;
import com.github.laxika.magicalvibes.cards.n.NezumiBladeblesser;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfRestlessShadows.class, MoonCircuitHacker.class, DeathcultRogue.class,
        NezumiBladeblesser.class, Forest.class})
class KamiOfRestlessShadowsTest extends BaseCardTest {

    @Test
    void returnsTargetNinjaFromGraveyardToHand() {
        Card ninja = new MoonCircuitHacker();
        harness.setGraveyard(player1, List.of(ninja));
        castAndChooseFirstMode();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(ninja.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ninja.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Moon-Circuit Hacker");
        harness.assertNotInGraveyard(player1, "Moon-Circuit Hacker");
    }

    @Test
    void returnsTargetRogueFromGraveyardToHand() {
        Card rogue = new DeathcultRogue();
        harness.setGraveyard(player1, List.of(rogue));
        castAndChooseFirstMode();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(rogue.getId());
        harness.handleMultipleCardsChosen(player1, List.of(rogue.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Deathcult Rogue");
        harness.assertNotInGraveyard(player1, "Deathcult Rogue");
    }

    @Test
    void firstModeCanBeDeclined() {
        castAndChooseFirstMode();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Kami of Restless Shadows");
    }

    @Test
    void firstModeCannotTargetOtherCreatureCards() {
        Card creature = new NezumiBladeblesser();
        harness.setGraveyard(player1, List.of(creature));
        castAndChooseFirstMode();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Nezumi Bladeblesser");
    }

    @Test
    void putsTargetCreatureFromGraveyardOnTopOfLibrary() {
        Card creature = new NezumiBladeblesser();
        Card libraryCard = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, new KamiOfRestlessShadows(), "{4}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put target creature card from your graveyard on top of your library");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, libraryCard);
        harness.assertNotInGraveyard(player1, "Nezumi Bladeblesser");
    }

    @Test
    void secondModeCannotTargetNonCreatureCards() {
        Card nonCreature = new Forest();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.castFromHand(player1, new KamiOfRestlessShadows(), "{4}{B}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly(
                "Return up to one target Ninja or Rogue creature card from your graveyard to your hand");
        harness.handleListChoice(player1, choice.options().getFirst());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void choosesModeAfterCreatureEntersRatherThanWhileCasting() {
        Card creature = new NezumiBladeblesser();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new KamiOfRestlessShadows(), "{4}{B}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Kami of Restless Shadows");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kami of Restless Shadows");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Put target creature card from your graveyard on top of your library");
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
        harness.assertNotInGraveyard(player1, "Nezumi Bladeblesser");
    }

    @Test
    void canChooseLibraryModeWhenEnteringWithoutBeingCast() {
        Card creature = new NezumiBladeblesser();
        harness.setGraveyard(player1, List.of(creature));
        harness.enterBattlefieldAndReturn(player1, new KamiOfRestlessShadows());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Put target creature card from your graveyard on top of your library");
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
        harness.assertNotInGraveyard(player1, "Nezumi Bladeblesser");
    }

    @Test
    void firstModeCanChooseZeroTargetsEvenWithEligibleCards() {
        Card ninja = new MoonCircuitHacker();
        harness.setGraveyard(player1, List.of(ninja));
        castAndChooseFirstMode();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Moon-Circuit Hacker");
        harness.assertNotInHand(player1, "Moon-Circuit Hacker");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void firstModeOnlyOffersCardsFromControllersGraveyard() {
        Card ownNinja = new MoonCircuitHacker();
        Card opposingNinja = new MoonCircuitHacker();
        harness.setGraveyard(player1, List.of(ownNinja));
        harness.setGraveyard(player2, List.of(opposingNinja));
        castAndChooseFirstMode();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(ownNinja.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownNinja.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Moon-Circuit Hacker");
        harness.assertInGraveyard(player2, "Moon-Circuit Hacker");
    }

    @Test
    void targetRemovedBeforeResolutionIsNotReturned() {
        Card ninja = new MoonCircuitHacker();
        harness.setGraveyard(player1, List.of(ninja));
        castAndChooseFirstMode();
        harness.handleMultipleCardsChosen(player1, List.of(ninja.getId()));

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Moon-Circuit Hacker");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndChooseFirstMode() {
        harness.castFromHand(player1, new KamiOfRestlessShadows(), "{4}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Return up to one target Ninja or Rogue creature card from your graveyard to your hand");
    }
}
