package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagitekInfantry.class, LeoninScimitar.class, GrizzlyBears.class})
class MagitekInfantryTest extends BaseCardTest {

    @Test
    void getsPlusOnePowerWithAnotherArtifact() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new MagitekInfantry());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, infantry)).isEqualTo(1);
    }

    @Test
    void doesNotCountItselfAsAnotherArtifact() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new MagitekInfantry());

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, infantry)).isEqualTo(1);
    }

    @Test
    void searchesForMagitekInfantryAndPutsItOntoBattlefieldTapped() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new MagitekInfantry());
        infantry.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new MagitekInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).allMatch(card -> card.getName().equals("Magitek Infantry"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Magitek Infantry"))
                .hasSize(2)
                .anyMatch(Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opposingArtifactDoesNotGrantBonus() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new MagitekInfantry());
        harness.addToBattlefield(player2, new MagitekInfantry());

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, infantry)).isEqualTo(1);
    }

    @Test
    void multipleOtherArtifactsGrantOnlyOneBonus() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new MagitekInfantry());
        harness.addToBattlefield(player1, new MagitekInfantry());
        harness.addToBattlefield(player1, new MagitekInfantry());

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, infantry)).isEqualTo(1);
    }

    @Test
    void anotherNonartifactCreatureDoesNotGrantBonus() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new MagitekInfantry());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, infantry)).isEqualTo(1);
    }

    @Test
    void bonusDisappearsWhenLastOtherArtifactLeaves() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new MagitekInfantry());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MagitekInfantry());
        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(other);

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, infantry)).isEqualTo(1);
    }

    @Test
    void searchWithNoMatchingCardFinishesWithoutAddingPermanent() {
        harness.addToBattlefield(player1, new MagitekInfantry());
        GrizzlyBears nonmatching = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonmatching));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canSearchWhileTappedAndSummoningSick() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new MagitekInfantry());
        infantry.tap();
        infantry.setSummoningSick(true);
        MagitekInfantry reinforcement = new MagitekInfantry();
        harness.setLibrary(player1, List.of(reinforcement));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(reinforcement.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.isTapped()).isTrue();
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
                });
        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canFailToFindEvenWhenMatchingCardExists() {
        harness.addToBattlefield(player1, new MagitekInfantry());
        MagitekInfantry reinforcement = new MagitekInfantry();
        harness.setLibrary(player1, List.of(reinforcement));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(reinforcement);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
