package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvenChorus.class, GrizzlyBears.class, Shock.class, Opalescence.class})
class ElvenChorusTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a creature spell from the top of the controller's library")
    void castsCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new ElvenChorus());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Controlled creatures can tap for one mana of any color")
    void givesControlledCreaturesAnyColorManaAbility() {
        harness.addToBattlefield(player1, new ElvenChorus());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast a noncreature spell from the top of the library")
    void cannotCastNoncreatureFromTop() {
        harness.addToBattlefield(player1, new ElvenChorus());
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    @Test
    @DisplayName("Does not grant top-library casting to an opponent")
    void onlyControllerMayCastFromLibraryTop() {
        harness.addToBattlefield(player1, new ElvenChorus());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Does not grant the mana ability to noncreatures")
    void doesNotGrantManaAbilityToNoncreatures() {
        harness.addToBattlefield(player1, new ElvenChorus());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void privatelyShowsNoncreatureTopCardDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new ElvenChorus());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Shock"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Shock"));
    }

    @Test
    void cannotCastCreatureWithoutPayingMana() {
        harness.addToBattlefield(player1, new ElvenChorus());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void doesNotAllowCreatureCastingDuringUpkeep() {
        harness.addToBattlefield(player1, new ElvenChorus());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void doesNotGrantManaAbilityToOpponentsCreatures() {
        harness.addToBattlefield(player1, new ElvenChorus());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void grantedTapAbilityRespectsSummoningSickness() {
        harness.addToBattlefield(player1, new ElvenChorus());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void grantsManaAbilityToItselfWhenItBecomesACreature() {
        Permanent chorus = addCreatureReady(player1, new ElvenChorus());
        harness.addToBattlefield(player1, new Opalescence());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(chorus.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
