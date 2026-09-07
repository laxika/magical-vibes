package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaktosTheUnscarred.class, GrizzlyBears.class, GrayOgre.class, HillGiant.class})
class HaktosTheUnscarredTest extends BaseCardTest {

    @Test
    @DisplayName("Haktos must attack each combat when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new HaktosTheUnscarred());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Haktos randomly chooses 2, 3, or 4 and protects from every other mana value")
    void choosesManaValueAndProtectsFromOtherValues() {
        Permanent haktos = castAndResolve();
        int chosenNumber = haktos.getChosenNumber();

        assertThat(chosenNumber).isIn(2, 3, 4);

        Map<Integer, Card> cardsByManaValue = Map.of(
                2, new GrizzlyBears(),
                3, new GrayOgre(),
                4, new HillGiant());
        for (Map.Entry<Integer, Card> entry : cardsByManaValue.entrySet()) {
            Permanent source = new Permanent(entry.getValue());
            assertThat(gqs.hasProtectionFromSource(gd, haktos, source))
                    .as("mana value %s", entry.getKey())
                    .isEqualTo(entry.getKey() != chosenNumber);
        }
    }

    @Test
    @DisplayName("Haktos has no mana-value protection if no number was chosen")
    void hasNoProtectionWithoutChosenNumber() {
        Permanent haktos = addCreatureReady(player1, new HaktosTheUnscarred());

        assertThat(gqs.hasProtectionFromSource(gd, haktos, new Permanent(new GrizzlyBears())))
                .isFalse();
    }

    private Permanent castAndResolve() {
        harness.setHand(player1, List.of(new HaktosTheUnscarred()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        return findPermanent(player1, "Haktos the Unscarred");
    }
}
