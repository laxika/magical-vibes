package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KioraTheRisingTideTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws two cards, then discards two cards")
    void entersAndLoots() {
        harness.setHand(player1, List.of(new KioraTheRisingTide(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Threshold attack trigger may create a legendary 8/8 blue Octopus token")
    void thresholdAttackCreatesToken() {
        addKioraWithGraveyard(7);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Scion of the Deep"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(8);
        assertThat(token.getCard().getToughness()).isEqualTo(8);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.OCTOPUS);
        assertThat(token.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Threshold attack trigger does not fire below seven graveyard cards")
    void thresholdAttackDoesNotFireBelowThreshold() {
        addKioraWithGraveyard(6);

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the threshold trigger does not create a token")
    void decliningThresholdTriggerDoesNotCreateToken() {
        addKioraWithGraveyard(7);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Scion of the Deep"));
    }

    private Permanent addKioraWithGraveyard(int graveyardSize) {
        Permanent kiora = addCreatureReady(player1, new KioraTheRisingTide());
        List<com.github.laxika.magicalvibes.model.Card> graveyard = new ArrayList<>();
        for (int i = 0; i < graveyardSize; i++) {
            graveyard.add(new GrizzlyBears());
        }
        harness.setGraveyard(player1, graveyard);
        return kiora;
    }
}
