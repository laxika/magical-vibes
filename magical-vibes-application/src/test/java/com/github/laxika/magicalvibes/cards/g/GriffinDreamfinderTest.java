package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GriffinDreamfinderTest extends BaseCardTest {

    private void castGriffinDreamfinder() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GriffinDreamfinder()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted enchantment card from graveyard to hand")
    void etbReturnsEnchantmentToHand() {
        AuraOfSilence aura = new AuraOfSilence();
        harness.setGraveyard(player1, List.of(aura));

        castGriffinDreamfinder();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Aura of Silence");
        harness.assertNotInGraveyard(player1, "Aura of Silence");
    }

    @Test
    @DisplayName("A nonenchantment card in the graveyard is not a legal target")
    void nonEnchantmentIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castGriffinDreamfinder();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
