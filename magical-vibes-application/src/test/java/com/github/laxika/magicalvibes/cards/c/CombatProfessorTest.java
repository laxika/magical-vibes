package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CombatProfessor.class, EagerFirstYear.class})
class CombatProfessorTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Beginning of combat boosts a creature you control and grants vigilance")
    void beginningOfCombatBuffsTargetCreature() {
        harness.addToBattlefield(player1, new CombatProfessor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void buffsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new CombatProfessor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new CombatProfessor());
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new CombatProfessor());
        harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat Professor can target itself")
    void canTargetItself() {
        Permanent professor = harness.addToBattlefieldAndReturn(player1, new CombatProfessor());
        int power = gqs.getEffectivePower(gd, professor);
        int toughness = gqs.getEffectiveToughness(gd, professor);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, professor.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, professor)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, professor)).isEqualTo(toughness);
        assertThat(gqs.hasKeyword(gd, professor, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The trigger resolves after Combat Professor leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent professor = harness.addToBattlefieldAndReturn(player1, new CombatProfessor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(professor);
        gd.playerGraveyards.get(player1.getId()).add(professor.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Neither effect applies if the target changes controller before resolution")
    void targetMustStillBeControlledOnResolution() {
        harness.addToBattlefield(player1, new CombatProfessor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

}
