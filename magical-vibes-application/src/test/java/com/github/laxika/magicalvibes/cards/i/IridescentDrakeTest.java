package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        HolyStrength holyStrength = new HolyStrength();
        harness.setGraveyard(player1, List.of(holyStrength));

        castIridescentDrake();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(holyStrength.getId()));
        harness.passBothPriorities();

        Permanent drake = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Iridescent Drake"))
                .findFirst()
                .orElseThrow();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Holy Strength"))
                .findFirst()
                .orElseThrow();

        assertThat(aura.getAttachedTo()).isEqualTo(drake.getId());
        harness.assertNotInGraveyard(player1, "Holy Strength");
    }

    @Test
    @DisplayName("ETB can target an Aura in an opponent's graveyard")
    void etbCanTargetOpponentsGraveyard() {
        HolyStrength holyStrength = new HolyStrength();
        harness.setGraveyard(player2, List.of(holyStrength));

        castIridescentDrake();

        harness.handleMultipleCardsChosen(player1, List.of(holyStrength.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Holy Strength");
        harness.assertNotInGraveyard(player2, "Holy Strength");
    }

    @Test
    @DisplayName("A non-Aura card in a graveyard is not a legal target")
    void nonAuraNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castIridescentDrake();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
