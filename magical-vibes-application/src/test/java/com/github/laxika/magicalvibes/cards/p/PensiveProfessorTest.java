package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PensiveProfessorTest extends BaseCardTest {

    @Test
    @DisplayName("Has counter-put draw trigger (Increment is keyword-driven)")
    void hasCorrectEffects() {
        PensiveProfessor card = new PensiveProfessor();

        assertThat(card.getEffects(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT).getFirst())
                .isInstanceOf(DrawCardEffect.class);
    }

    @Test
    @DisplayName("Increment placing a +1/+1 counter triggers a card draw")
    void incrementCounterDrawsCard() {
        Permanent professor = harness.addToBattlefieldAndReturn(player1, new PensiveProfessor());
        professor.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast a spell spending 2 mana — greater than the 0/2's power (0), so Increment fires.
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        GameData localGd = harness.getGameData();
        int handBefore = localGd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Increment resolves, puts a +1/+1 counter on the Professor
        harness.passBothPriorities(); // counter-put trigger resolves, drawing a card

        assertThat(professor.getPlusOnePlusOneCounters()).isEqualTo(1);
        // Cast Grizzly Bears left hand empty (handBefore - 1), then the trigger drew one back.
        assertThat(localGd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
