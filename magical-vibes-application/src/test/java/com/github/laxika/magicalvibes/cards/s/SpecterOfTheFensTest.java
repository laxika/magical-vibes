package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Flunk;
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

@CardUsed({SpecterOfTheFens.class})
class SpecterOfTheFensTest extends BaseCardTest {

    @Test
    @DisplayName("Ability makes an opponent lose 2 life and its controller gain 2 life")
    void opponentLosesLifeAndControllerGainsLife() {
        harness.addToBattlefield(player1, new SpecterOfTheFens());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addMana();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Ability cannot target its controller")
    void cannotTargetController() {
        harness.addToBattlefield(player1, new SpecterOfTheFens());
        addMana();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick on an opponent's turn")
    void activatesWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent specter = harness.addToBattlefieldAndReturn(player1, new SpecterOfTheFens());
        specter.setTapped(true);
        specter.setSummoningSick(true);
        addMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(specter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can be activated twice before either activation resolves")
    void activatesTwiceInResponseToItself() {
        harness.addToBattlefield(player1, new SpecterOfTheFens());
        addMana();
        addMana();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Ability requires the full six mana activation cost")
    void cannotActivateWithOnlyFiveMana() {
        harness.addToBattlefield(player1, new SpecterOfTheFens());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ability requires black mana even when six generic mana are available")
    void cannotActivateWithoutBlackMana() {
        harness.addToBattlefield(player1, new SpecterOfTheFens());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life gain belongs to the activating controller when the other player controls the Specter")
    void otherControllerGainsLife() {
        harness.addToBattlefield(player2, new SpecterOfTheFens());
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }

    @Test
    @CardUsed({Flunk.class})
    @DisplayName("Ability still resolves after its source dies in response")
    void resolvesAfterSourceDies() {
        Permanent specter = harness.addToBattlefieldAndReturn(player1, new SpecterOfTheFens());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Flunk()));
        addMana();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.castAndResolveInstant(player2, 0, specter.getId());
        harness.assertInGraveyard(player1, "Specter of the Fens");
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
