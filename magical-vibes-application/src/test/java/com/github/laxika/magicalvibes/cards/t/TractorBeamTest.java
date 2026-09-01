package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TractorBeam.class, GrizzlyBears.class})
class TractorBeamTest extends BaseCardTest {

    @Test
    @DisplayName("Taps and gains control of the enchanted creature")
    void tapsAndControlsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castAndResolve(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Can enchant, tap, and control a Spacecraft")
    void controlsSpacecraft() {
        Permanent spacecraft = harness.addToBattlefieldAndReturn(player2, spacecraft());

        castAndResolve(spacecraft);

        assertThat(spacecraft.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spacecraft);
    }

    @Test
    @DisplayName("The enchanted permanent does not untap during its controller's untap step")
    void enchantedPermanentDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castAndResolve(creature);
        advanceToNextTurn(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a permanent that is neither a creature nor a Spacecraft")
    void cannotEnchantOtherPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, otherPermanent());
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or Spacecraft");
    }

    private void castAndResolve(Permanent target) {
        prepareCast();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new TractorBeam()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
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

    private Card spacecraft() {
        Card card = new Card();
        card.setName("Test Spacecraft");
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.SPACECRAFT));
        return card;
    }

    private Card otherPermanent() {
        Card card = new Card();
        card.setName("Test Land");
        card.setType(CardType.LAND);
        return card;
    }
}
