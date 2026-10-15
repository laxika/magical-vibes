package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Fling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LaeliaTheBladeReforged;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DanceWithCalamity.class, AvatarOfMight.class, Forest.class, GrizzlyBears.class,
        LaeliaTheBladeReforged.class, Fling.class})
class DanceWithCalamityTest extends BaseCardTest {

    @Test
    @DisplayName("Can stop exiling immediately and leave the library unchanged")
    void canStopWithoutExiling() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        castDanceWithCalamity();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Exiles repeatedly, then offers exiled spells for free casting within the limit")
    void exilesAndCastsWithinManaValueLimit() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears, new Forest(), new Forest(), new Forest()));

        castDanceWithCalamity();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(castChoice.validCardIds()).containsExactly(bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Does not offer free casts when the exiled total exceeds thirteen")
    void exceedingManaValueLimitPreventsCasting() {
        AvatarOfMight first = new AvatarOfMight();
        AvatarOfMight second = new AvatarOfMight();
        harness.setLibrary(player1, List.of(first, second));

        castDanceWithCalamity();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(first.getId())
                || entry.getCard().getId().equals(second.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void exactlyThirteenAllowsCastingAllExiledSpells() {
        AvatarOfMight avatar = new AvatarOfMight();
        LaeliaTheBladeReforged laelia = new LaeliaTheBladeReforged();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(avatar, laelia, bears));

        castDanceWithCalamity();
        for (int i = 0; i < 3; i++) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.handleMultipleCardsChosen(player1, List.of(avatar.getId(), laelia.getId(), bears.getId()));

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack.stream().map(entry -> entry.getCard().getId()))
                .contains(avatar.getId(), laelia.getId(), bears.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Laelia, the Blade Reforged");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Avatar of Might");
    }

    @Test
    void stoppingEarlyLeavesUncastCardsExiledAndRestInLibrary() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        castDanceWithCalamity();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(first.getId())
                || entry.getCard().getId().equals(second.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryCompletesWithoutChoices() {
        harness.setLibrary(player1, List.of());

        castDanceWithCalamity();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Dance with Calamity");
    }

    @Test
    void exiledLandsRemainExiledWithoutBeingPlayed() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        castDanceWithCalamity();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .containsExactly(forest.getId());
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void spellWithPayableAdditionalCostMustNotBeSkipped() {
        Fling fling = new Fling();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(fling));

        castDanceWithCalamity();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(fling.getId()));

        // Casting Fling must request its sacrifice and target choices rather than silently skip it.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void canKeepExilingAfterExceedingThirteen() {
        AvatarOfMight first = new AvatarOfMight();
        AvatarOfMight second = new AvatarOfMight();
        AvatarOfMight third = new AvatarOfMight();
        harness.setLibrary(player1, List.of(first, second, third));

        castDanceWithCalamity();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canCastOnlySomeExiledSpellsAndCannotCastPreviouslyExiledCards() {
        GrizzlyBears chosen = new GrizzlyBears();
        GrizzlyBears unchosen = new GrizzlyBears();
        AvatarOfMight previouslyExiled = new AvatarOfMight();
        harness.setExile(player1, List.of(previouslyExiled));
        harness.setLibrary(player1, List.of(chosen, unchosen));

        castDanceWithCalamity();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), unchosen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .containsExactlyInAnyOrder(unchosen.getId(), previouslyExiled.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
    private void castDanceWithCalamity() {
        harness.castFromHand(player1, new DanceWithCalamity(), "{7}{R}");
        harness.passBothPriorities();
    }
}
