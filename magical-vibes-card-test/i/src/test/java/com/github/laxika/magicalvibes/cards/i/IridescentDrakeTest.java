package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.cards.m.MaskOfLawAndGrace;
import com.github.laxika.magicalvibes.cards.f.FesteringWound;
import com.github.laxika.magicalvibes.cards.r.Rescue;
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

@CardUsed({IridescentDrake.class, IlluminatedWings.class, MetathranSoldier.class,
        MaskOfLawAndGrace.class, FesteringWound.class, Rescue.class})
class IridescentDrakeTest extends BaseCardTest {

    private void castIridescentDrake() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new IridescentDrake(), "{3}{U}");
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

    @Test
    @DisplayName("Drake enters normally when both graveyards are empty")
    void emptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        castIridescentDrake();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Iridescent Drake");
    }

    @Test
    @DisplayName("Aura stays in its graveyard when the Drake leaves before resolution")
    void auraStaysInGraveyardWhenSourceLeaves() {
        IlluminatedWings wings = new IlluminatedWings();
        harness.setGraveyard(player2, List.of(wings));
        castIridescentDrake();
        harness.handleMultipleCardsChosen(player1, List.of(wings.getId()));
        Permanent drake = findPermanent(player1, "Iridescent Drake");
        harness.setHand(player1, List.of(new Rescue()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, drake.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Iridescent Drake");
        harness.assertInGraveyard(player2, "Illuminated Wings");
        harness.assertNotOnBattlefield(player1, "Illuminated Wings");
    }

    @Test
    @DisplayName("Protection prevents the Aura from entering at all")
    void protectionLeavesAuraInOriginalGraveyardWithoutReentering() {
        FesteringWound wound = new FesteringWound();
        harness.setGraveyard(player2, List.of(wound));
        long originalGraveyardEntry = gd.graveyardEntryVersion(wound.getId());
        castIridescentDrake();
        Permanent drake = findPermanent(player1, "Iridescent Drake");
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MaskOfLawAndGrace());
        mask.setAttachedTo(drake.getId());
        harness.handleMultipleCardsChosen(player1, List.of(wound.getId()));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Festering Wound");
        harness.assertNotOnBattlefield(player1, "Festering Wound");
        assertThat(gd.graveyardEntryVersion(wound.getId())).isEqualTo(originalGraveyardEntry);
    }
}
