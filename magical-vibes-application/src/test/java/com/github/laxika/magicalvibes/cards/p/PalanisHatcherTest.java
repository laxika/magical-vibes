package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PalanisHatcher.class)
class PalanisHatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two 0/1 green Dinosaur Egg tokens that have haste")
    void entersWithEggTokens() {
        castAndResolveEtb();

        List<Permanent> eggs = eggs(player1);
        assertThat(eggs).hasSize(2);
        assertThat(eggs).allSatisfy(egg -> {
            assertThat(egg.getCard().getPower()).isZero();
            assertThat(egg.getCard().getToughness()).isEqualTo(1);
            assertThat(egg.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(egg.getCard().getSubtypes())
                    .contains(CardSubtype.DINOSAUR, CardSubtype.EGG);
            assertThat(gqs.hasKeyword(gd, egg, Keyword.HASTE)).isTrue();
        });
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Palani's Hatcher"), Keyword.HASTE))
                .isFalse();
    }

    @Test
    @DisplayName("At the beginning of combat, sacrifices an Egg and creates a 3/3 Dinosaur")
    void sacrificesEggAndCreatesDinosaurAtBeginningOfCombat() {
        castAndResolveEtb();
        Permanent sacrificedEgg = eggs(player1).getFirst();

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(sacrificedEgg.getId()));

        assertThat(eggs(player1)).hasSize(1);
        List<Permanent> dinosaurs = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.DINOSAUR))
                .filter(permanent -> !permanent.getCard().getSubtypes().contains(CardSubtype.EGG))
                .toList();
        assertThat(dinosaurs).hasSize(1);
        assertThat(dinosaurs.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(dinosaurs.getFirst().getCard().getToughness()).isEqualTo(3);
        assertThat(dinosaurs.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(gqs.hasKeyword(gd, dinosaurs.getFirst(), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not create a Dinosaur when no Egg is controlled")
    void noEggMeansNoBeginningOfCombatToken() {
        harness.addToBattlefield(player1, new PalanisHatcher());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new PalanisHatcher()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> eggs(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.EGG))
                .toList();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
