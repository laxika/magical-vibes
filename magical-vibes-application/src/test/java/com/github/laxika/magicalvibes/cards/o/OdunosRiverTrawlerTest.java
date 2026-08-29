package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornEidolon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OdunosRiverTrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted enchantment creature card from the graveyard to hand")
    void etbReturnsEnchantmentCreatureToHand() {
        NyxbornEidolon eidolon = new NyxbornEidolon();
        harness.setGraveyard(player1, List.of(eidolon));

        castTrawler();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(eidolon.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nyxborn Eidolon");
        harness.assertNotInGraveyard(player1, "Nyxborn Eidolon");
    }

    @Test
    @DisplayName("ETB does not target a non-enchantment creature card")
    void etbRejectsNonEnchantmentCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castTrawler();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Activation sacrifices the creature and returns an enchantment creature card")
    void activationReturnsEnchantmentCreatureToHand() {
        OdunosRiverTrawler trawler = new OdunosRiverTrawler();
        NyxbornEidolon eidolon = new NyxbornEidolon();
        addCreatureReady(player1, trawler);
        harness.setGraveyard(player1, List.of(eidolon));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(eidolon.getId()));
        harness.assertInGraveyard(player1, "Odunos River Trawler");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nyxborn Eidolon");
        harness.assertNotInGraveyard(player1, "Nyxborn Eidolon");
    }

    @Test
    @DisplayName("Activation cannot target a non-enchantment creature card")
    void activationRejectsNonEnchantmentCreature() {
        addCreatureReady(player1, new OdunosRiverTrawler());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTrawler() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OdunosRiverTrawler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
