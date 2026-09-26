package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KiraGreatGlassSpinner;
import com.github.laxika.magicalvibes.cards.r.RoarOfJukai;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GoryosVengeance.class, KiraGreatGlassSpinner.class, GnarledMass.class, RoarOfJukai.class})
class GoryosVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a legendary creature card from your graveyard with haste")
    void returnsLegendaryCreatureWithHaste() {
        Card legend = legendaryCreature();
        harness.setGraveyard(player1, List.of(legend));
        harness.setHand(player1, List.of(new GoryosVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, legend.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kira, Great Glass-Spinner");
        harness.assertNotInGraveyard(player1, "Kira, Great Glass-Spinner");

        Permanent creature = findPermanent(player1, "Kira, Great Glass-Spinner");
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("The returned creature is exiled at the beginning of the next end step")
    void exiledAtNextEndStep() {
        Card legend = legendaryCreature();
        harness.setGraveyard(player1, List.of(legend));
        harness.setHand(player1, List.of(new GoryosVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, legend.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Kira, Great Glass-Spinner");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kira, Great Glass-Spinner"));
    }

    @Test
    @DisplayName("Cannot target a nonlegendary creature card")
    void cannotTargetNonlegendaryCreature() {
        Card creature = new GnarledMass();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GoryosVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a legendary creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card legend = legendaryCreature();
        harness.setGraveyard(player2, List.of(legend));
        harness.setHand(player1, List.of(new GoryosVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, legend.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card legend = legendaryCreature();
        harness.setGraveyard(player1, List.of(legend));
        harness.setHand(player1, List.of(new GoryosVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, legend.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Kira, Great Glass-Spinner");
    }

    @Test
    @DisplayName("Splices the reanimation effect onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Card legend = legendaryCreature();
        harness.setGraveyard(player1, List.of(legend));
        harness.setHand(player1, List.of(new RoarOfJukai(), new GoryosVengeance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithSplice(player1, 0, legend.getId(), List.of(1));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kira, Great Glass-Spinner");
        harness.assertInHand(player1, "Goryo's Vengeance");
        harness.assertInGraveyard(player1, "Roar of Jukai");
    }

    private Card legendaryCreature() {
        return new KiraGreatGlassSpinner();
    }
}
