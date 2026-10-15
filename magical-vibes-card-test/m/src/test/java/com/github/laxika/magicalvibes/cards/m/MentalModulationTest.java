package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EmergencyEject;
import com.github.laxika.magicalvibes.cards.i.IntrepidTenderfoot;
import com.github.laxika.magicalvibes.cards.n.NutrientBlock;
import com.github.laxika.magicalvibes.cards.w.WeaponsManufacturing;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MentalModulation.class, IntrepidTenderfoot.class, NutrientBlock.class,
        WeaponsManufacturing.class, EmergencyEject.class})
class MentalModulationTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target creature and draws a card")
    void tapsCreatureAndDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        Card drawnCard = new IntrepidTenderfoot();
        harness.setLibrary(player1, List.of(drawnCard));

        cast(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Can target an artifact")
    void tapsArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NutrientBlock());

        cast(target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WeaponsManufacturing());
        harness.setHand(player1, List.of(new MentalModulation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Costs only blue mana during its controller's turn")
    void costsOneLessDuringYourTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        harness.setHand(player1, List.of(new MentalModulation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not get the cost reduction during an opponent's turn")
    void costsFullAmountDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        harness.setHand(player1, List.of(new MentalModulation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Still draws when the target is already tapped")
    void drawsWhenTargetAlreadyTapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        target.tap();
        Card drawnCard = new IntrepidTenderfoot();
        harness.setLibrary(player1, List.of(drawnCard));

        cast(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Can tap its controller's creature and draws for the caster")
    void tapsOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        Card drawnCard = new IntrepidTenderfoot();
        harness.setLibrary(player1, List.of(drawnCard));

        cast(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Does not draw when its only target leaves before resolution")
    void doesNotDrawWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        Card undrawnCard = new IntrepidTenderfoot();
        harness.setLibrary(player1, List.of(undrawnCard));
        harness.setHand(player1, List.of(new MentalModulation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new EmergencyEject()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawnCard);
        harness.assertInGraveyard(player1, "Mental Modulation");
        harness.assertInGraveyard(player2, "Intrepid Tenderfoot");
    }

    @Test
    @DisplayName("Resolves for its full cost on an opponent's turn")
    void resolvesDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        Card drawnCard = new IntrepidTenderfoot();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        cast(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The reduction cannot replace the required blue mana")
    void stillRequiresBlueManaDuringYourTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        harness.setHand(player1, List.of(new MentalModulation()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new MentalModulation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
