package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SunseedNurturerTest extends BaseCardTest {

    // ===== End-step life gain =====

    @Test
    @DisplayName("Gains 2 life at end step when controlling a power-5-or-greater creature and accepting")
    void gainsLifeWhenControllingBigCreature() {
        harness.addToBattlefield(player1, new SunseedNurturer());
        harness.addToBattlefield(player1, new AvatarOfMight()); // 8/8
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Declining the may ability gains no life")
    void decliningGainsNoLife() {
        harness.addToBattlefield(player1, new SunseedNurturer());
        harness.addToBattlefield(player1, new AvatarOfMight()); // 8/8
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger without a power-5-or-greater creature")
    void noTriggerWithoutBigCreature() {
        harness.addToBattlefield(player1, new SunseedNurturer()); // 1/1 only
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapAddsColorlessMana() {
        addCreatureReady(player1, new SunseedNurturer());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Helpers =====

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN -> END_STEP, triggers fire
    }
}
