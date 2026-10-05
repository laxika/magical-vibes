package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FaerieSeer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OuraTheImitator.class, DarkRitual.class, FaerieSeer.class, GrizzlyBears.class})
class OuraTheImitatorTest extends BaseCardTest {

    @Test
    void castsInstantFromTopOfLibrary() {
        harness.addToBattlefield(player1, new OuraTheImitator());
        DarkRitual ritual = new DarkRitual();
        harness.setLibrary(player1, List.of(ritual));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertInGraveyard(player1, "Dark Ritual");
    }

    @Test
    void castsFaerieFromTopOfLibrary() {
        harness.addToBattlefield(player1, new OuraTheImitator());
        FaerieSeer faerie = new FaerieSeer();
        harness.setLibrary(player1, List.of(faerie));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Faerie Seer");
    }

    @Test
    void faerieSpellsHaveFlash() {
        harness.addToBattlefield(player1, new OuraTheImitator());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FaerieSeer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotCastNonFaerieNonInstantFromTopOfLibrary() {
        harness.addToBattlefield(player1, new OuraTheImitator());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void cannotLookAtNonFaerieNonInstantOnTop() {
        harness.addToBattlefield(player1, new OuraTheImitator());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Grizzly Bears"));
    }

    @Test
    void instantOnTopIsVisibleOnlyToControllerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new OuraTheImitator());
        harness.setLibrary(player1, List.of(new DarkRitual()));
        harness.forceActivePlayer(player2);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Dark Ritual"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Dark Ritual"));
    }

    @Test
    void faerieOnTopIsVisibleOnlyToController() {
        harness.addToBattlefield(player1, new OuraTheImitator());
        harness.setLibrary(player1, List.of(new FaerieSeer()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Faerie Seer"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Faerie Seer"));
    }

    @Test
    void castsFaerieFromLibraryOnOpponentsTurn() {
        harness.addToBattlefield(player1, new OuraTheImitator());
        harness.setLibrary(player1, List.of(new FaerieSeer()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Faerie Seer");
    }

    @Test
    void libraryCastingPermissionEndsWhenOuraLeaves() {
        harness.addToBattlefield(player1, new OuraTheImitator());
        DarkRitual ritual = new DarkRitual();
        harness.setLibrary(player1, List.of(ritual));
        harness.addMana(player1, ManaColor.BLACK, 1);
        gd.playerBattlefields.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ritual);
    }

    @Test
    void ouraInLibraryDoesNotRevealItselfWithoutBattlefieldPermission() {
        harness.setLibrary(player1, List.of(new OuraTheImitator()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains("Oura, the Imitator"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Oura, the Imitator"));
    }
}
