package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenSunsZenith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PrecognitionField;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.cards.r.Refurbish;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.cards.s.SplendidReclamation;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeatheredRunestone.class, ThinkTwice.class, RiseFromTheGrave.class,
        GrizzlyBears.class, GreenSunsZenith.class, LlanowarElves.class, RampantGrowth.class,
        Forest.class, PrecognitionField.class, Shock.class, Refurbish.class, SongOfTheDryads.class,
        SplendidReclamation.class})
class WeatheredRunestoneTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents flashback casting from graveyards")
    void preventsFlashbackCasting() {
        harness.addToBattlefield(player1, new WeatheredRunestone());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        setupPlayer2Active();
        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Blocks nonland permanents returning from graveyards")
    void blocksNonlandPermanentFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new WeatheredRunestone());
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blocks nonland permanents entering from libraries")
    void blocksNonlandPermanentFromLibrary() {
        harness.addToBattlefield(player1, new WeatheredRunestone());
        harness.setHand(player1, List.of(new GreenSunsZenith()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Allows lands to enter from libraries")
    void allowsLandsFromLibrary() {
        harness.addToBattlefield(player1, new WeatheredRunestone());
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Prevents casting a spell from the top of a library")
    void preventsCastingFromLibraryTop() {
        harness.addToBattlefield(player1, new PrecognitionField());
        harness.addToBattlefield(player1, new WeatheredRunestone());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("libraries");
    }

    @Test
    @DisplayName("Blocks noncreature artifacts returning from a graveyard")
    void blocksArtifactFromGraveyard() {
        Card artifact = new WeatheredRunestone();
        harness.addToBattlefield(player2, new WeatheredRunestone());
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new Refurbish()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Weathered Runestone");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Losing printed abilities allows artifacts to return from graveyards")
    void losingAbilitiesAllowsReanimation() {
        Permanent runestone = harness.addToBattlefieldAndReturn(player2, new WeatheredRunestone());
        Card artifact = new WeatheredRunestone();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new SongOfTheDryads(), new Refurbish()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, runestone.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasLostAllAbilities(gd, runestone)).isTrue();

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Weathered Runestone");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("Allows creature spells cast from hand")
    void allowsCreatureFromHand() {
        harness.addToBattlefield(player2, new WeatheredRunestone());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Also prevents its controller casting from their graveyard")
    void preventsControllersFlashbackCasting() {
        harness.addToBattlefield(player1, new WeatheredRunestone());
        Card spell = new ThinkTwice();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Allows lands to return from graveyards")
    void allowsLandsFromGraveyard() {
        harness.addToBattlefield(player2, new WeatheredRunestone());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new SplendidReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Losing printed abilities allows creatures to enter from libraries")
    void losingAbilitiesAllowsLibraryEntry() {
        Permanent runestone = harness.addToBattlefieldAndReturn(player2, new WeatheredRunestone());
        harness.setHand(player1, List.of(new SongOfTheDryads(), new GreenSunsZenith()));
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castEnchantment(player1, 0, runestone.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasLostAllAbilities(gd, runestone)).isTrue();

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Losing printed abilities allows spells to be cast from libraries")
    void losingAbilitiesAllowsLibraryCasting() {
        Permanent runestone = harness.addToBattlefieldAndReturn(player2, new WeatheredRunestone());
        harness.addToBattlefield(player1, new PrecognitionField());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, runestone.getId());
        harness.passBothPriorities();

        harness.castFromLibraryTop(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
