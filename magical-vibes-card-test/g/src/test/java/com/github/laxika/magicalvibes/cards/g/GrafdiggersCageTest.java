package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.p.PrecognitionField;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.o.OracleOfMulDaya;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulSummons;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrafdiggersCage.class, GrizzlyBears.class, PrecognitionField.class,
        RiseFromTheGrave.class, Shock.class, ThinkTwice.class, SoulSummons.class,
        MarchOfTheMachines.class, TurnToFrog.class, GreenSunsZenith.class,
        DryadArbor.class, OracleOfMulDaya.class})
class GrafdiggersCageTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents flashback casting from graveyards while on the battlefield")
    void preventsFlashbackCasting() {
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        setupPlayer2Active();
        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback indices are empty while Grafdigger's Cage is on the battlefield")
    void flashbackIndicesAreEmpty() {
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        setupPlayer2Active();
        List<Integer> playable = harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player2.getId());
        assertThat(playable).isEmpty();
    }

    @Test
    @DisplayName("Blocks reanimation: a creature card stays in the graveyard")
    void blocksReanimationFromGraveyard() {
        harness.setGraveyard(player1, List.of(testCreature()));
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        // The creature card could not enter the battlefield and stays in the graveyard.
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Without the Cage, the same reanimation puts the creature onto the battlefield")
    void reanimationWorksWithoutCage() {
        harness.setGraveyard(player1, List.of(testCreature()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevents casting a spell from the top of a library")
    void preventsCastingFromLibraryTop() {
        harness.addToBattlefield(player1, new PrecognitionField());
        harness.addToBattlefield(player1, new GrafdiggersCage());
        gd.playerDecks.get(player1.getId()).addFirst(new Shock());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("libraries");
    }

    @Test
    @DisplayName("Without the Cage, casting from the top of a library is allowed")
    void castingFromLibraryTopWorksWithoutCage() {
        harness.addToBattlefield(player1, new PrecognitionField());
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);
        harness.addMana(player1, ManaColor.RED, 1);

        var bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatCode(() -> harness.castFromLibraryTop(player1, bearsId)).doesNotThrowAnyException();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    void blocksCreatureEnteringFromLibrary() {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new GreenSunsZenith()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void blocksManifestingCreatureFromLibrary() {
        assertManifestBlocked(new GrizzlyBears());
    }

    @Test
    void blocksManifestingNoncreatureFromLibrary() {
        assertManifestBlocked(new Shock());
    }

    private void assertManifestBlocked(Card topCard) {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new SoulSummons()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void allowsReanimationAfterCageLosesItsAbilities() {
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new TurnToFrog(), new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grafdigger's Cage"));
        harness.passBothPriorities();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void preventsPlayingCreatureLandFromLibrary() {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        Card arbor = new DryadArbor();
        harness.setLibrary(player1, List.of(arbor));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotOnBattlefield(player1, "Dryad Arbor");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(arbor);
    }

    private Card testCreature() {
        return new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
