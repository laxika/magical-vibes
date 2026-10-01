package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DuergarAssailant;
import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BallynockTrapper.class, DuergarAssailant.class, DuskdaleWurm.class})
class BallynockTrapperTest extends BaseCardTest {

    @BeforeEach
    void setUp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("{T}: Tap target creature taps the chosen creature")
    void tapAbilityTapsTarget() {
        addCreatureReady(player1, new BallynockTrapper());
        Permanent target = addCreatureReady(player2, new DuskdaleWurm());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the tap ability taps the Trapper")
    void activatingTapsSelf() {
        Permanent trapper = addCreatureReady(player1, new BallynockTrapper());
        Permanent target = addCreatureReady(player2, new DuskdaleWurm());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(trapper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a white spell and accepting untaps the tapped Trapper")
    void whiteSpellUntapsTrapper() {
        Permanent trapper = addCreatureReady(player1, new BallynockTrapper());
        Permanent target = addCreatureReady(player2, new DuskdaleWurm());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(trapper.isTapped()).isTrue();

        harness.castFromHand(player1, new BallynockTrapper(), "{3}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(trapper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the white-spell trigger leaves the Trapper tapped")
    void decliningLeavesTrapperTapped() {
        Permanent trapper = addCreatureReady(player1, new BallynockTrapper());
        Permanent target = addCreatureReady(player2, new DuskdaleWurm());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(trapper.isTapped()).isTrue();

        harness.castFromHand(player1, new BallynockTrapper(), "{3}{W}");

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(trapper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-white spell does not trigger the untap")
    void nonWhiteSpellDoesNotTrigger() {
        addCreatureReady(player1, new BallynockTrapper());

        harness.castFromHand(player1, new DuskdaleWurm(), "{5}{G}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Casting a multicolored white spell triggers the untap")
    void multicoloredWhiteSpellUntapsTrapper() {
        Permanent trapper = addCreatureReady(player1, new BallynockTrapper());
        Permanent target = addCreatureReady(player2, new DuskdaleWurm());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(trapper.isTapped()).isTrue();

        harness.setHand(player1, List.of(new DuergarAssailant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(trapper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's white spell does not trigger the untap")
    void opponentWhiteSpellDoesNotTrigger() {
        Permanent trapper = addCreatureReady(player1, new BallynockTrapper());
        Permanent target = addCreatureReady(player2, new DuskdaleWurm());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(trapper.isTapped()).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new BallynockTrapper(), "{3}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(trapper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability rejects a non-creature target")
    void tapAbilityRejectsNonCreatureTarget() {
        addCreatureReady(player1, new BallynockTrapper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
