package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitnessOfTomorrows.class})
class WitnessOfTomorrowsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability pays {3}{U} without tapping Witness of Tomorrows")
    void activatingPaysManaWithoutTapping() {
        Permanent witness = addReadyWitness();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(witness.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving the ability starts a one-card scry")
    void resolvingStartsScryOne() {
        addReadyWitness();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1);
    }

    @Test
    @DisplayName("Scrying can put the top card on the bottom of the library")
    void scryCanBottomTopCard() {
        Permanent witness = addReadyWitness();
        witness.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Card> library = gd.playerDecks.get(player1.getId());
        Card top = library.getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(library.getFirst()).isNotSameAs(top);
        assertThat(library.getLast()).isSameAs(top);
    }

    @Test
    @DisplayName("The ability cannot be activated without {3}{U}")
    void cannotActivateWithoutMana() {
        addReadyWitness();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyWitness() {
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new WitnessOfTomorrows());
        witness.setSummoningSick(false);
        return witness;
    }
}
