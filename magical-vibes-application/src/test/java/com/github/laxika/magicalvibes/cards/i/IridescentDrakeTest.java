package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
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

@CardUsed({IridescentDrake.class, IlluminatedWings.class, MetathranSoldier.class})
class IridescentDrakeTest extends BaseCardTest {

    private void castIridescentDrake() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IridescentDrake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted Aura from a graveyard and attaches it to Iridescent Drake")
    void etbReturnsAuraAndAttachesItToSource() {
        IlluminatedWings illuminatedWings = new IlluminatedWings();
        harness.setGraveyard(player1, List.of(illuminatedWings));

        castIridescentDrake();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(illuminatedWings.getId()));
        harness.passBothPriorities();

        Permanent drake = findPermanent(player1, "Iridescent Drake");
        Permanent aura = findPermanent(player1, "Illuminated Wings");

        assertThat(aura.getAttachedTo()).isEqualTo(drake.getId());
        harness.assertNotInGraveyard(player1, "Illuminated Wings");
    }

    @Test
    @DisplayName("ETB can target an Aura in an opponent's graveyard")
    void etbCanTargetOpponentsGraveyard() {
        IlluminatedWings illuminatedWings = new IlluminatedWings();
        harness.setGraveyard(player2, List.of(illuminatedWings));

        castIridescentDrake();

        harness.handleMultipleCardsChosen(player1, List.of(illuminatedWings.getId()));
        harness.passBothPriorities();

        Permanent drake = findPermanent(player1, "Iridescent Drake");
        Permanent aura = findPermanent(player1, "Illuminated Wings");
        assertThat(aura.getAttachedTo()).isEqualTo(drake.getId());
        harness.assertNotInGraveyard(player2, "Illuminated Wings");
    }

    @Test
    @DisplayName("A non-Aura card in a graveyard is not a legal target")
    void nonAuraNotTargetable() {
        harness.setGraveyard(player1, List.of(new MetathranSoldier()));

        castIridescentDrake();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Iridescent Drake");
        harness.assertInGraveyard(player1, "Metathran Soldier");
    }
}
