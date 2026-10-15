package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(JasconianIsle.class)
class JasconianIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JasconianIsle()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Jasconian Isle").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping a ready Isle adds one blue mana")
    void tapsForBlueMana() {
        Permanent isle = addReadyIsle();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(isle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness prevents its mana ability")
    void summoningSicknessPreventsManaAbility() {
        harness.addToBattlefield(player1, new JasconianIsle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent isle = addReadyIsle();
        isle.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(isle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {U}{U} during upkeep untaps it")
    void payingUntapsIt() {
        Permanent isle = addReadyIsle();
        isle.tap();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(isle.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Declining upkeep payment leaves both the Isle and available mana unchanged")
    void decliningPaymentKeepsMana() {
        Permanent isle = addReadyIsle();
        isle.tap();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(isle.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent isle = addReadyIsle();
        isle.tap();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(isle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped Isle still offers upkeep payment")
    void untappedIsleStillTriggers() {
        Permanent isle = addReadyIsle();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(isle.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private Permanent addReadyIsle() {
        return addCreatureReady(player1, new JasconianIsle());
    }
}
