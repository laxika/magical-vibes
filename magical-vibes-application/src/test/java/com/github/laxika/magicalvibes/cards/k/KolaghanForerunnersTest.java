package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KolaghanForerunners.class)
class KolaghanForerunnersTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures its controller controls")
    void powerEqualsControlledCreatures() {
        Permanent forerunners = addForerunners(player1);

        assertThat(gqs.getEffectivePower(gd, forerunners)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forerunners)).isEqualTo(3);

        Permanent otherForerunners = addForerunners(player1);
        addForerunners(player2);

        assertThat(gqs.getEffectivePower(gd, forerunners)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherForerunners)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power updates when creatures leave its controller's battlefield")
    void powerUpdatesWhenCreaturesChange() {
        Permanent forerunners = addForerunners(player1);
        addForerunners(player1);

        assertThat(gqs.getEffectivePower(gd, forerunners)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent != forerunners);

        assertThat(gqs.getEffectivePower(gd, forerunners)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dash grants haste and returns the creature to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new KolaghanForerunners()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent forerunners = findPermanent(player1, "Kolaghan Forerunners");
        assertThat(forerunners.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kolaghan Forerunners");
        harness.assertNotOnBattlefield(player1, "Kolaghan Forerunners");
    }

    private Permanent addForerunners(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new KolaghanForerunners());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
