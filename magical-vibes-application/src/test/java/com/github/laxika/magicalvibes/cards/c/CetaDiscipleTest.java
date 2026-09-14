package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelfireCrusader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CetaDisciple.class, AngelfireCrusader.class, CetaSanctuary.class})
class CetaDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Red ability gives target creature +2/+0 until end of turn")
    void redAbilityStrengthensTargetCreature() {
        Permanent disciple = addReadyDisciple(player1);
        Permanent target = addCreatureReady(player1, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(disciple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Red ability can target a creature an opponent controls")
    void redAbilityCanTargetOpponentCreature() {
        addReadyDisciple(player1);
        Permanent target = addCreatureReady(player2, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Green ability adds one mana of the chosen color")
    void greenAbilityAddsAnyColorMana() {
        Permanent disciple = addReadyDisciple(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(disciple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped disciple cannot activate another ability")
    void cannotActivateAbilityWhileTapped() {
        Permanent disciple = addReadyDisciple(player1);
        Permanent target = addCreatureReady(player1, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(disciple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addReadyDisciple(player1);
        Permanent target = addCreatureReady(player1, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The red ability cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addReadyDisciple(player1);
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new CetaSanctuary());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sanctuary.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDisciple(Player player) {
        return addCreatureReady(player, new CetaDisciple());
    }
}
