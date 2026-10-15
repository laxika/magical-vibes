package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.Starstorm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkApostle.class, DarkRitual.class, Divination.class, Forest.class, GrizzlyBears.class,
        MindStone.class, Starstorm.class})
class DarkApostleTest extends BaseCardTest {

    @Test
    void nextNoncreatureSpellHasCascadeAndCreatureSpellDoesNotConsumeIt() {
        setupApostle();
        harness.setLibrary(player1, List.of(new DarkRitual()));
        activateApostle();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Dark Ritual");
    }

    @Test
    void cascadeGrantIsOneShot() {
        setupApostle();
        harness.setLibrary(player1, List.of(
                new DarkRitual(), new Forest(), new Forest(), new Forest()));
        activateApostle();

        harness.setHand(player1, List.of(new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cascadeUsesAnnouncedXInTheNoncreatureSpellsManaValue() {
        setupApostle();
        harness.setLibrary(player1, List.of(new MindStone()));
        activateApostle();

        harness.setHand(player1, List.of(new Starstorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Mind Stone");
    }

    @Test
    void artifactSpellCascadesPastLandsAndCardsWithEqualOrGreaterManaValue() {
        setupApostle();
        var forest = new Forest();
        var equalCost = new MindStone();
        var expensive = new DarkApostle();
        var ritual = new DarkRitual();
        var untouched = new Forest();
        harness.setLibrary(player1, List.of(forest, equalCost, expensive, ritual, untouched));
        activateApostle();

        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(ritual);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dark Ritual");
        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(forest, equalCost, expensive);
    }

    @Test
    void grantPersistsAfterApostleLeavesTheBattlefield() {
        setupApostle();
        harness.setLibrary(player1, List.of(new DarkRitual()));
        activateApostle();
        harness.setHand(player2, List.of(new Starstorm()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, 3, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Dark Apostle");

        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName()).containsExactly("Dark Ritual");
    }

    @Test
    void opponentsSpellDoesNotConsumeTheGrant() {
        setupApostle();
        harness.setLibrary(player1, List.of(new DarkRitual()));
        activateApostle();

        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    void unusedGrantExpiresAtTheEndOfTheTurn() {
        setupApostle();
        activateApostle();
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setLibrary(player1, List.of(new DarkRitual()));
        harness.setHand(player1, List.of(new Starstorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Dark Ritual");
    }

    @Test
    void multipleActivationsGiveTheSameNextSpellMultipleCascades() {
        setupApostle();
        addCreatureReady(player1, new DarkApostle());
        harness.setLibrary(player1, List.of(new DarkRitual(), new DarkRitual()));
        activateApostle();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Dark Ritual", "Dark Ritual");
    }

    private void setupApostle() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        addCreatureReady(player1, new DarkApostle());
    }

    private void activateApostle() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
