package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.cards.r.RetracedImage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheLostAndTheDamned.class, Forest.class, GrizzlyBears.class, RampantGrowth.class, RetracedImage.class})
class TheLostAndTheDamnedTest extends BaseCardTest {

    @Test
    void landEnteringFromGraveyardCreatesSpawn() {
        harness.addToBattlefield(player1, new TheLostAndTheDamned());
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        gd.graveyardPlayPermissions.put(land.getId(), player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playGraveyardLand(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spawn")).hasSize(1);
    }

    @Test
    void spellCastFromExileCreatesSpawn() {
        harness.addToBattlefield(player1, new TheLostAndTheDamned());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spawn")).hasSize(1);
    }

    @Test
    void landEnteringFromHandDoesNotCreateSpawn() {
        harness.addToBattlefield(player1, new TheLostAndTheDamned());
        harness.setHand(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);

        assertThat(findPermanents(player1, "Spawn")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void spellCastFromHandDoesNotCreateSpawn() {
        harness.addToBattlefield(player1, new TheLostAndTheDamned());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Spawn")).isEmpty();
    }

    @Test
    void landPutOntoBattlefieldFromLibraryCreatesSpawn() {
        harness.addToBattlefield(player1, new TheLostAndTheDamned());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new RampantGrowth(), "{1}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanents(player1, "Spawn")).hasSize(1);
        assertThat(findPermanents(player2, "Spawn")).isEmpty();
    }

    @Test
    void opponentsLandEnteringFromGraveyardDoesNotCreateSpawn() {
        harness.addToBattlefield(player1, new TheLostAndTheDamned());
        Forest land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        gd.graveyardPlayPermissions.put(land.getId(), player2.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playGraveyardLand(player2, 0);

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spawn")).isEmpty();
    }

    @Test
    void opponentsSpellCastFromExileDoesNotCreateSpawn() {
        harness.addToBattlefield(player1, new TheLostAndTheDamned());
        GrizzlyBears spell = new GrizzlyBears();
        harness.setExile(player2, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromExile(player2, spell.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Spawn")).isEmpty();
    }

    @Test
    void landPutOntoBattlefieldFromHandDoesNotCreateSpawn() {
        harness.addToBattlefield(player1, new TheLostAndTheDamned());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RetracedImage(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spawn")).isEmpty();
    }
}
