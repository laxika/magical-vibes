package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LiesaShroudOfDusk.class, Fog.class})
class LiesaShroudOfDuskTest extends BaseCardTest {

    @Test
    @DisplayName("The player who casts a spell loses 2 life")
    void spellCasterLosesLife() {
        harness.addToBattlefield(player1, new LiesaShroudOfDusk());
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int casterLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(casterLifeBefore - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("The controller loses 2 life when they cast a spell")
    void controllerLosesLifeWhenCastingSpell() {
        harness.addToBattlefield(player1, new LiesaShroudOfDusk());
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }
}
