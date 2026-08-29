package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SpiritMirror;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AliriosEnraptured.class, SpiritMirror.class})
class AliriosEnrapturedTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and creates a 3/2 blue Reflection token")
    void entersTappedAndCreatesReflection() {
        Permanent alirios = castAlirios(player1);
        assertThat(alirios.isTapped()).isTrue();

        Permanent reflection = findPermanent(player1, "Reflection");
        assertThat(reflection.getEffectivePower()).isEqualTo(3);
        assertThat(reflection.getEffectiveToughness()).isEqualTo(2);
        assertThat(reflection.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(reflection.getCard().getSubtypes()).contains(CardSubtype.REFLECTION);
    }

    @Test
    @DisplayName("Does not untap while its controller controls a Reflection")
    void doesNotUntapWithReflection() {
        Permanent alirios = castAlirios(player1);

        advanceToNextTurn(player2);

        assertThat(alirios.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps normally without a Reflection under its controller's control")
    void untapsWithoutReflection() {
        Permanent alirios = addCreatureReady(player1, new AliriosEnraptured());
        alirios.tap();

        advanceToNextTurn(player2);

        assertThat(alirios.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Reflection does not prevent Alirios from untapping")
    void opponentReflectionDoesNotCount() {
        harness.addToBattlefield(player2, new SpiritMirror());
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        Permanent alirios = addCreatureReady(player1, new AliriosEnraptured());
        alirios.tap();

        advanceToNextTurn(player2);

        assertThat(alirios.isTapped()).isFalse();
    }

    private Permanent castAlirios(Player player) {
        harness.setHand(player, List.of(new AliriosEnraptured()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player, "Alirios, Enraptured");
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
