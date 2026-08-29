package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CombustionTechnique.class, AirbendingLesson.class, GrizzlyBears.class, HillGiant.class})
class CombustionTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage and marks the target for exile instead of dying")
    void dealsBaseDamageAndMarksTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(target.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Deals one additional damage for each Lesson in the caster's graveyard")
    void countsLessonsInCasterGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        cast(target);

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new CombustionTechnique()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
