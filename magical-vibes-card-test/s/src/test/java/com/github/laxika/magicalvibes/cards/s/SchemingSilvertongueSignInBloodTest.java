package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SchemingSilvertongueSignInBlood.class, SignInBlood.class})
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

    @Test
    void preparedSpellCanTargetOpponentAndUnpreparesOnCasting() {
        Permanent silvertongue = addCreatureReady(player1, new SchemingSilvertongueSignInBlood());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        advanceToPostcombatMain();
        harness.passBothPriorities();
        var copyId = silvertongue.getPreparedSpellCardId();
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, copyId, player2.getId());

        assertThat(silvertongue.isPrepared()).isFalse();
        assertThat(silvertongue.getPreparedSpellCardId()).isNull();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Sign in Blood");
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    @Test
    void preparedSpellCanTargetController() {
        Permanent silvertongue = addCreatureReady(player1, new SchemingSilvertongueSignInBlood());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        advanceToPostcombatMain();
        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, silvertongue.getPreparedSpellCardId(), player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void castingPreparedSpellRequiresItsManaCost() {
        Permanent silvertongue = addCreatureReady(player1, new SchemingSilvertongueSignInBlood());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        advanceToPostcombatMain();
        harness.passBothPriorities();
        var copyId = silvertongue.getPreparedSpellCardId();

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(silvertongue.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    void doesNotPrepareDuringOpponentsSecondMainPhase() {
        Permanent silvertongue = addCreatureReady(player1, new SchemingSilvertongueSignInBlood());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(silvertongue.isPrepared()).isFalse();
    }

    @Test
    void preparingAgainKeepsTheExistingSpellCopy() {
        Permanent silvertongue = addCreatureReady(player1, new SchemingSilvertongueSignInBlood());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        advanceToPostcombatMain();
        harness.passBothPriorities();
        var copyId = silvertongue.getPreparedSpellCardId();

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThat(silvertongue.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
    }
}
