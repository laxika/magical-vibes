package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Catalog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NephaliaAcademyTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new NephaliaAcademy());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent-caused discard goes on top of the library")
    void opponentCausedDiscardGoesOnTopOfLibrary() {
        harness.addToBattlefield(player2, new NephaliaAcademy());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RavenousRats()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("A self-caused discard still goes to the graveyard")
    void selfCausedDiscardStillGoesToGraveyard() {
        harness.addToBattlefield(player1, new NephaliaAcademy());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Catalog(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
