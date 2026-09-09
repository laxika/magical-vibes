package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchemingSilvertongueSignInBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared in the second main phase after gaining at least 2 life")
    void becomesPreparedAfterGainingTwoLife() {
        Permanent silvertongue = addCreatureReady(player1, new SchemingSilvertongueSignInBlood());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThat(silvertongue.isPrepared()).isTrue();
        assertThat(silvertongue.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.findExiledCard(silvertongue.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Does not become prepared without gaining at least 2 life")
    void doesNotBecomePreparedWithoutEnoughLifeGain() {
        Permanent silvertongue = addCreatureReady(player1, new SchemingSilvertongueSignInBlood());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(silvertongue.isPrepared()).isFalse();
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
    }
}
