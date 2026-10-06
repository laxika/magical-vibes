package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.i.InvasiveSpecies;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KalonianTwingrove;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RotfeasterMaggot.class, InvasiveSpecies.class, RuneclawBear.class, Negate.class,
        Forest.class, KalonianTwingrove.class})
class RotfeasterMaggotTest extends BaseCardTest {

    /** Casts Rotfeaster Maggot and resolves it so the ETB trigger sets up graveyard targeting. */
    private void castMaggot() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RotfeasterMaggot(), "{4}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles a creature card from an opponent's graveyard and gains its toughness in life")
    void exilesOpponentCreatureAndGainsLife() {
        InvasiveSpecies giant = new InvasiveSpecies(); // 3/3
        harness.setGraveyard(player2, List.of(giant));

        int lifeBefore = gd.getLife(player1.getId());
        castMaggot();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Invasive Species");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Invasive Species"));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("The controller's own graveyard is a legal source and the life gained scales with toughness")
    void exilesOwnCreatureAndGainsLife() {
        RuneclawBear bears = new RuneclawBear(); // 2/2
        harness.setGraveyard(player1, List.of(bears));

        int lifeBefore = gd.getLife(player1.getId());
        castMaggot();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("A noncreature card in a graveyard is not a legal target")
    void noncreatureCardNotTargetable() {
        harness.setGraveyard(player2, List.of(new Negate()));

        int lifeBefore = gd.getLife(player1.getId());
        castMaggot();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Negate");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("With no legal graveyard target, the ability is removed from the stack and gives no life")
    void noCreatureCardNoLifeGain() {
        int lifeBefore = gd.getLife(player1.getId());
        castMaggot();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Life gain uses the exiled card's characteristic-defining toughness")
    void gainsLifeFromCharacteristicDefiningToughness() {
        KalonianTwingrove twingrove = new KalonianTwingrove();
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setGraveyard(player2, List.of(twingrove));
        int lifeBefore = gd.getLife(player1.getId());

        castMaggot();
        harness.handleMultipleCardsChosen(player1, List.of(twingrove.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Kalonian Twingrove");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(twingrove);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("A target that leaves the graveyard before resolution gives no life")
    void missingTargetGivesNoLife() {
        RuneclawBear bears = new RuneclawBear();
        harness.setGraveyard(player2, List.of(bears));
        int lifeBefore = gd.getLife(player1.getId());

        castMaggot();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(bears));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player1, "Rotfeaster Maggot");
    }
}
