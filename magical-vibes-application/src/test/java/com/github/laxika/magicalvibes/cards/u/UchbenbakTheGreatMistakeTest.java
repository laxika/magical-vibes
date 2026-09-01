package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UchbenbakTheGreatMistake.class, Forest.class})
class UchbenbakTheGreatMistakeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard with a finality counter when descended eight")
    void returnsFromGraveyardWithFinalityCounter() {
        prepareMainPhase();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        harness.setGraveyard(player1, graveyardWithAdditionalPermanents(uchbenbak, 7));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(uchbenbak.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Uchbenbak, the Great Mistake");
    }

    @Test
    @DisplayName("Cannot activate without eight permanent cards in the graveyard")
    void cannotActivateWithoutEightPermanentCards() {
        prepareMainPhase();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        harness.setGraveyard(player1, graveyardWithAdditionalPermanents(uchbenbak, 6));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Uchbenbak, the Great Mistake");
    }

    @Test
    @DisplayName("Cannot activate outside sorcery timing")
    void cannotActivateOutsideSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        harness.setGraveyard(player1, graveyardWithAdditionalPermanents(uchbenbak, 7));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private List<Card> graveyardWithAdditionalPermanents(Card source, int additionalPermanents) {
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(source);
        for (int i = 0; i < additionalPermanents; i++) {
            graveyard.add(new Forest());
        }
        return graveyard;
    }
}
