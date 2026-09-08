package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarrionGrubTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+0 from the greatest creature-card power in its controller's graveyard")
    void boostsByGreatestCreaturePowerInOwnGraveyard() {
        Permanent grub = addCreatureReady(player1, new CarrionGrub());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new ColossalDreadmaw(), new Forest()));
        harness.setGraveyard(player2, List.of(new CarnageTyrant()));

        assertThat(gqs.getEffectivePower(gd, grub)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, grub)).isEqualTo(5);

        gd.playerGraveyards.get(player1.getId()).removeIf(card -> card instanceof ColossalDreadmaw);

        assertThat(gqs.getEffectivePower(gd, grub)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters by milling four cards")
    void entersByMillingFourCards() {
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new CarrionGrub()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(library);
        harness.assertOnBattlefield(player1, "Carrion Grub");
    }
}
