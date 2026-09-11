package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SkySpirit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenegadeWarlord.class, SkySpirit.class})
class RenegadeWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking pumps each other attacking creature but not itself")
    void pumpsOtherAttackers() {
        Permanent warlord = addCreatureReady(player1, new RenegadeWarlord());
        Permanent otherAttacker = addCreatureReady(player1, new SkySpirit());
        Permanent stayHome = addCreatureReady(player1, new SkySpirit());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, stayHome)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new RenegadeWarlord());
        Permanent otherAttacker = addCreatureReady(player1, new SkySpirit());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each attacking Warlord boosts the other attackers once")
    void multipleWarlordsStackTheirTriggers() {
        Permanent firstWarlord = addCreatureReady(player1, new RenegadeWarlord());
        Permanent secondWarlord = addCreatureReady(player1, new RenegadeWarlord());
        Permanent otherAttacker = addCreatureReady(player1, new SkySpirit());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstWarlord)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondWarlord)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(4);
    }
}
