package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishInfantry.class, BenalishKnight.class})
class BenalishInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("Benalish Infantry can form a band with one non-banding creature")
    void canFormBandWithNonBandingCreature() {
        Permanent infantry = addCreatureReady(player1, new BenalishInfantry());
        Permanent knight = addCreatureReady(player1, new BenalishKnight());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));

        assertThat(infantry.getBandId()).isNotNull();
        assertThat(infantry.getBandId()).isEqualTo(knight.getBandId());
    }
}
