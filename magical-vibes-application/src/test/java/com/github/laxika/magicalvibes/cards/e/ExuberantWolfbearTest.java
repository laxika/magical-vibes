package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExuberantWolfbear.class, EliteVanguard.class, GrizzlyBears.class})
class ExuberantWolfbearTest extends BaseCardTest {

    @Test
    @DisplayName("The attack trigger sets a Human's base power and toughness to the Wolfbear's actual values")
    void setsHumanBasePowerAndToughnessToActualSourceValues() {
        Permanent wolfbear = addCreatureReady(player1, new ExuberantWolfbear());
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        wolfbear.setPowerModifier(2);
        wolfbear.setToughnessModifier(-1);
        human.setPowerModifier(1);
        human.setToughnessModifier(1);

        resolveAttackTrigger(human, true);

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(4);
    }

    @Test
    @DisplayName("The attack trigger may be declined")
    void mayBeDeclined() {
        addCreatureReady(player1, new ExuberantWolfbear());
        Permanent human = addCreatureReady(player1, new EliteVanguard());

        resolveAttackTrigger(human, false);

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger can target only a Human the controller controls")
    void targetsOnlyHumanControllerControls() {
        Permanent wolfbear = addCreatureReady(player1, new ExuberantWolfbear());
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent opponentHuman = addCreatureReady(player2, new EliteVanguard());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(human.getId());
        assertThat(choice.validIds()).doesNotContain(wolfbear.getId(), opponentHuman.getId(), bears.getId());

        harness.handlePermanentChosen(player1, human.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("The attack trigger uses the Wolfbear's last-known power and toughness")
    void usesLastKnownSourceValues() {
        Permanent wolfbear = addCreatureReady(player1, new ExuberantWolfbear());
        Permanent human = addCreatureReady(player1, new EliteVanguard());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, human.getId());
        gd.playerBattlefields.get(player1.getId()).remove(wolfbear);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(4);
    }

    @Test
    @DisplayName("The base power and toughness change ends at end of turn")
    void expiresAtEndOfTurn() {
        addCreatureReady(player1, new ExuberantWolfbear());
        Permanent human = addCreatureReady(player1, new EliteVanguard());

        resolveAttackTrigger(human, true);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }

    private void resolveAttackTrigger(Permanent human, boolean accept) {
        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, human.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accept);
    }
}
