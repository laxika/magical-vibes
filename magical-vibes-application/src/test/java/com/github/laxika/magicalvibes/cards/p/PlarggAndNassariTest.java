package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LindblumIndustrialRegency;
import com.github.laxika.magicalvibes.cards.m.MageSiege;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlarggAndNassari.class, Forest.class, GrizzlyBears.class})
class PlarggAndNassariTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles through nonlands, excludes the opponent's choice, and offers the other spell")
    void exilesThroughNonlandsAndOffersTheOtherSpell() {
        Forest player1Land = new Forest();
        GrizzlyBears player1FirstSpell = new GrizzlyBears();
        Forest player2Land = new Forest();
        GrizzlyBears player2Spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Land, player1FirstSpell));
        harness.setLibrary(player2, List.of(player2Land, player2Spell));
        harness.addToBattlefield(player1, new PlarggAndNassari());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PlarggAndNassariCardChoice opponentChoice =
                (PendingInteraction.PlarggAndNassariCardChoice) gd.interaction.activeInteraction();
        assertThat(opponentChoice.opponentId()).isEqualTo(player2.getId());
        assertThat(opponentChoice.validCardIds()).containsExactlyInAnyOrder(
                player1FirstSpell.getId(), player2Spell.getId());
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(player1Land, player1FirstSpell, player2Land, player2Spell);

        harness.handleMultipleCardsChosen(player2, List.of(player2Spell.getId()));

        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(castChoice.validCardIds()).containsExactlyInAnyOrder(
                player1FirstSpell.getId());
        assertThat(castChoice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(player1FirstSpell.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard())
                .containsExactly(player1FirstSpell);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .contains(player2Spell, player1Land, player2Land);
    }

    @Test
    void mayDeclineToCastAndAllCardsRemainExiled() {
        GrizzlyBears firstSpell = new GrizzlyBears();
        GrizzlyBears secondSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstSpell));
        harness.setLibrary(player2, List.of(secondSpell));
        harness.addToBattlefield(player1, new PlarggAndNassari());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(firstSpell.getId()));
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(firstSpell, secondSpell);
    }

    @Test
    void castsOpponentsCreatureForFreeUnderAbilityControllersControl() {
        GrizzlyBears firstSpell = new GrizzlyBears();
        GrizzlyBears secondSpell = new GrizzlyBears();
        GrizzlyBears unexiledSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstSpell, unexiledSpell));
        harness.setLibrary(player2, List.of(secondSpell));
        harness.addToBattlefield(player1, new PlarggAndNassari());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(firstSpell.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(secondSpell.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard()).containsExactly(secondSpell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unexiledSpell);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(secondSpell.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).containsExactly(firstSpell);
    }

    @Test
    void onlyNonlandIsExcludedWhenOtherLibraryContainsOnlyLands() {
        Forest land = new Forest();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of(spell));
        harness.addToBattlefield(player1, new PlarggAndNassari());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PlarggAndNassariCardChoice choice =
                (PendingInteraction.PlarggAndNassariCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(spell.getId());
        harness.handleMultipleCardsChosen(player2, List.of(spell.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(land, spell);
    }

    @Test
    void noChoiceWhenNeitherLibraryContainsANonland() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new PlarggAndNassari());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).containsExactly(land);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        GrizzlyBears firstSpell = new GrizzlyBears();
        GrizzlyBears secondSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstSpell));
        harness.setLibrary(player2, List.of(secondSpell));
        harness.addToBattlefield(player1, new PlarggAndNassari());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstSpell);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondSpell);
    }

    @Test
    @CardUsed({LindblumIndustrialRegency.class, MageSiege.class})
    void offersAdventureOfExiledLandAlongsideRemainingNonland() {
        LindblumIndustrialRegency land = new LindblumIndustrialRegency();
        GrizzlyBears firstSpell = new GrizzlyBears();
        GrizzlyBears secondSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, firstSpell));
        harness.setLibrary(player2, List.of(secondSpell));
        harness.addToBattlefield(player1, new PlarggAndNassari());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PlarggAndNassariCardChoice opponentChoice =
                (PendingInteraction.PlarggAndNassariCardChoice) gd.interaction.activeInteraction();
        assertThat(opponentChoice.validCardIds())
                .containsExactlyInAnyOrder(firstSpell.getId(), secondSpell.getId());
        harness.handleMultipleCardsChosen(player2, List.of(firstSpell.getId()));

        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(castChoice.validCardIds())
                .containsExactlyInAnyOrder(land.getId(), secondSpell.getId());
        assertThat(castChoice.maxCount()).isEqualTo(2);
    }
}
